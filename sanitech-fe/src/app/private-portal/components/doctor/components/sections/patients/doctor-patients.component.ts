import { Component, OnInit, OnDestroy, HostListener, Renderer2 } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { forkJoin, of, Subject } from 'rxjs';
import { catchError, map, takeUntil } from 'rxjs/operators';
import {
  DoctorApiService,
  PatientDto,
  ConsentScope,
  AppointmentDto,
  Page
} from '../../../services/doctor-api.service';
import { Patient } from './dtos/patients.dto';

@Component({
  selector: 'app-doctor-patients',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './doctor-patients.component.html',
  styleUrls: ['./doctor-patients.component.css']
})
export class DoctorPatientsComponent implements OnInit, OnDestroy {
  // Cleanup subscription
  private destroy$ = new Subject<void>();

  // Lista pazienti
  patients: Patient[] = [];

  // Filtri
  searchQuery = '';
  consentFilter: 'ALL' | ConsentScope = 'ALL';
  viewMode: 'consented' | 'all' = 'consented';

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  // Stato UI
  isLoading = false;
  isRefreshing = false;
  selectedPatient: Patient | null = null;
  showPatientModal = false;
  errorMessage = '';
  consentWarning = false;

  constructor(
    private doctorApi: DoctorApiService,
    private renderer: Renderer2
  ) {}

  // Statistiche
  get totalWithConsent(): number {
    return this.patients.filter(p => p.hasActiveConsent).length;
  }

  get totalPatients(): number {
    return this.patients.length;
  }

  get patientsWithDocs(): number {
    return this.patients.filter(p => p.consents.includes('DOCS')).length;
  }

  get patientsWithPrescriptions(): number {
    return this.patients.filter(p => p.consents.includes('PRESCRIPTIONS')).length;
  }

  get patientsWithTelevisits(): number {
    return this.patients.filter(p => p.consents.includes('TELEVISIT')).length;
  }

  ngOnInit(): void {
    this.loadPatients();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    // Rimuovi overflow hidden dal body se il modale era aperto
    this.renderer.removeClass(document.body, 'overflow-hidden');
  }

  // ========================================================================
  // BUG-005 FIX: Usa getPatientsWithConsent (batch) invece di N+1
  // ========================================================================
  loadPatients(): void {
    this.isLoading = true;
    this.isRefreshing = this.patients.length > 0;
    this.errorMessage = '';
    this.consentWarning = false;

    const doctorId = this.doctorApi.getDoctorId();

    // Carica pazienti + consensi batch + appuntamenti in parallelo
    forkJoin({
      patientsPage: this.doctorApi.searchPatients({ size: 100 }),
      docsConsents: this.doctorApi.getPatientsWithConsent('DOCS').pipe(
        catchError(() => { this.consentWarning = true; return of([] as number[]); })
      ),
      prescConsents: this.doctorApi.getPatientsWithConsent('PRESCRIPTIONS').pipe(
        catchError(() => { this.consentWarning = true; return of([] as number[]); })
      ),
      televisitConsents: this.doctorApi.getPatientsWithConsent('TELEVISIT').pipe(
        catchError(() => { this.consentWarning = true; return of([] as number[]); })
      ),
      appointments: this.doctorApi.searchAppointments({
        doctorId: doctorId ?? undefined,
        size: 100
      }).pipe(
        catchError(() => of({ content: [], totalElements: 0, totalPages: 0, size: 0, number: 0 } as Page<AppointmentDto>))
      )
    }).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: ({ patientsPage, docsConsents, prescConsents, televisitConsents, appointments }) => {
        const patientDtos = patientsPage.content || [];
        const appointmentsList = appointments.content || [];

        // Costruisci mappa consensi per paziente
        const consentMap = new Map<number, ConsentScope[]>();
        docsConsents.forEach(pid => {
          const arr = consentMap.get(pid) || [];
          arr.push('DOCS');
          consentMap.set(pid, arr);
        });
        prescConsents.forEach(pid => {
          const arr = consentMap.get(pid) || [];
          arr.push('PRESCRIPTIONS');
          consentMap.set(pid, arr);
        });
        televisitConsents.forEach(pid => {
          const arr = consentMap.get(pid) || [];
          arr.push('TELEVISIT');
          consentMap.set(pid, arr);
        });

        // Costruisci mappa prossimo appuntamento per paziente
        const now = new Date();
        const nextAppointmentMap = new Map<number, string>();
        appointmentsList
          .filter(a => a.status === 'BOOKED' && new Date(a.startAt) > now)
          .sort((a, b) => new Date(a.startAt).getTime() - new Date(b.startAt).getTime())
          .forEach(a => {
            if (!nextAppointmentMap.has(a.patientId)) {
              nextAppointmentMap.set(a.patientId, a.startAt);
            }
          });

        this.patients = patientDtos.map(dto =>
          this.mapPatient(dto, consentMap.get(dto.id) || [], nextAppointmentMap.get(dto.id))
        );
        this.isLoading = false;
        this.isRefreshing = false;
      },
      error: () => {
        this.errorMessage = 'Errore nel caricamento dei pazienti. Riprova.';
        this.isLoading = false;
        this.isRefreshing = false;
      }
    });
  }

  // ========================================================================
  // BUG-004 FIX: Popola nextAppointment dalla API appuntamenti
  // BUG-008 FIX: Null-safe mapping
  // ========================================================================
  private mapPatient(dto: PatientDto, consents: ConsentScope[], nextAppointment?: string): Patient {
    return {
      id: dto.id,
      firstName: dto.firstName || '',
      lastName: dto.lastName || '',
      fiscalCode: dto.fiscalCode || '',
      email: dto.email || '',
      phone: dto.phone || '',
      birthDate: dto.birthDate || '',
      consents,
      nextAppointment: nextAppointment || undefined,
      hasActiveConsent: consents.length > 0
    };
  }

  // ========================================================================
  // BUG-008 FIX: Null-safe search filter
  // ========================================================================
  get filteredPatients(): Patient[] {
    let filtered = this.patients;

    // Filtro per vista (con consenso / tutti)
    if (this.viewMode === 'consented') {
      filtered = filtered.filter(p => p.hasActiveConsent);
    }

    // Filtro per tipo consenso
    if (this.consentFilter !== 'ALL') {
      filtered = filtered.filter(p => p.consents.includes(this.consentFilter as ConsentScope));
    }

    // Filtro ricerca (null-safe)
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(p =>
        (p.firstName || '').toLowerCase().includes(query) ||
        (p.lastName || '').toLowerCase().includes(query) ||
        (p.fiscalCode || '').toLowerCase().includes(query)
      );
    }

    return filtered;
  }

  get paginatedPatients(): Patient[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredPatients.slice(start, start + this.pageSize);
  }

  // ========================================================================
  // BUG-020 FIX: totalPages = 0 quando non ci sono risultati
  // ========================================================================
  get totalPages(): number {
    return Math.ceil(this.filteredPatients.length / this.pageSize);
  }

  // ========================================================================
  // BUG-018 FIX: Paginazione con ellissi (max 7 pulsanti visibili)
  // ========================================================================
  get visiblePages(): (number | '...')[] {
    const total = this.totalPages;
    const current = this.currentPage;
    const maxVisible = 7;

    if (total <= maxVisible) {
      return Array.from({ length: total }, (_, i) => i + 1);
    }

    const pages: (number | '...')[] = [];

    // Sempre mostra la prima pagina
    pages.push(1);

    if (current > 3) {
      pages.push('...');
    }

    // Pagine vicine alla corrente
    const start = Math.max(2, current - 1);
    const end = Math.min(total - 1, current + 1);
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }

    if (current < total - 2) {
      pages.push('...');
    }

    // Sempre mostra l'ultima pagina
    if (total > 1) {
      pages.push(total);
    }

    return pages;
  }

  // ========================================================================
  // BUG-014/015/016 FIX: Modale con Escape, backdrop click, body scroll lock
  // ========================================================================
  openPatientModal(patient: Patient): void {
    this.selectedPatient = patient;
    this.showPatientModal = true;
    this.renderer.addClass(document.body, 'overflow-hidden');
  }

  closePatientModal(): void {
    this.showPatientModal = false;
    this.selectedPatient = null;
    this.renderer.removeClass(document.body, 'overflow-hidden');
  }

  @HostListener('document:keydown.escape')
  onEscapeKey(): void {
    if (this.showPatientModal) {
      this.closePatientModal();
    }
  }

  onBackdropClick(event: MouseEvent): void {
    // Chiudi solo se il click è direttamente sul backdrop (non sul contenuto del modale)
    if ((event.target as HTMLElement).classList.contains('modal')) {
      this.closePatientModal();
    }
  }

  // ========================================================================
  // BUG-006 FIX: Supporta tutti gli scope consenso incluso RECORDS (future-proof)
  // ========================================================================
  getConsentLabel(scope: ConsentScope): string {
    const labels: Record<string, string> = {
      DOCS: 'Documenti',
      PRESCRIPTIONS: 'Prescrizioni',
      TELEVISIT: 'Televisite'
    };
    return labels[scope] || scope;
  }

  getConsentBadgeClass(scope: ConsentScope): string {
    const classes: Record<string, string> = {
      DOCS: 'bg-primary bg-opacity-10 text-primary',
      PRESCRIPTIONS: 'bg-success bg-opacity-10 text-success',
      TELEVISIT: 'bg-info bg-opacity-10 text-info'
    };
    return classes[scope] || 'bg-secondary bg-opacity-10 text-secondary';
  }

  // ========================================================================
  // BUG-011 FIX: Maschera il codice fiscale per pazienti senza consenso
  // ========================================================================
  getMaskedFiscalCode(patient: Patient): string {
    if (!patient.fiscalCode) return '';
    if (patient.hasActiveConsent) return patient.fiscalCode;
    // Mostra solo i primi 6 caratteri (nome/cognome codifica)
    const fc = patient.fiscalCode;
    if (fc.length <= 6) return fc;
    return fc.substring(0, 6) + '****' + fc.substring(fc.length - 1);
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return '-';
    return d.toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  formatDateTime(dateStr: string | undefined): string {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return '-';
    return d.toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  // ========================================================================
  // BUG-007 FIX: Restituisce null se la data di nascita è mancante
  // ========================================================================
  calculateAge(birthDate: string): number | null {
    if (!birthDate) return null;
    const birth = new Date(birthDate);
    if (isNaN(birth.getTime())) return null;
    const today = new Date();
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
      age--;
    }
    return age;
  }

  // ========================================================================
  // BUG-022 FIX: Messaggi empty state contestuali
  // ========================================================================
  get emptyStateMessage(): string {
    if (this.searchQuery.trim()) {
      return 'Nessun paziente trovato con i criteri di ricerca.';
    }
    if (this.viewMode === 'consented' && this.consentFilter !== 'ALL') {
      return 'Nessun paziente ha concesso il consenso per questa categoria.';
    }
    if (this.viewMode === 'consented') {
      return 'Nessun paziente ti ha ancora concesso il consenso. I pazienti possono farlo dalla loro app.';
    }
    return 'Non ci sono pazienti nel tuo reparto.';
  }

  get emptyStateIcon(): string {
    if (this.searchQuery.trim()) return 'bi-search';
    if (this.viewMode === 'consented') return 'bi-shield-exclamation';
    return 'bi-people';
  }
}
