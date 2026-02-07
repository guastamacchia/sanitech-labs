import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { PaymentsService } from './payments.service';
import {
  PaymentItem,
  PaymentStats,
  PaymentTypeEnum,
  ServicePerformedItem,
  ServicePerformedStats,
  ServiceCreatePayload,
  ServiceEditPayload,
  DoctorItem,
  PatientItem,
  FacilityItem,
  DepartmentItem
} from './dtos/payments.dto';

@Component({
  selector: 'app-admin-payments',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-payments.component.html'
})
export class AdminPaymentsComponent implements OnInit {

  // Stato generale
  isLoading = false;

  // --- Payment Orders ---
  payments: PaymentItem[] = [];
  paymentsError = '';
  paymentsSuccess = '';
  paymentStats: PaymentStats = {
    totalPayments: 0,
    completedWithin7Days: 0,
    completedWithReminder: 0,
    stillPending: 0,
    percentWithin7Days: 94,
    percentWithReminder: 4,
    percentPending: 2
  };
  paymentStatusFilter: 'ALL' | 'CREATED' | 'CAPTURED' | 'FAILED' | 'CANCELLED' | 'REFUNDED' = 'ALL';
  paymentDaysFilter: number | null = null;
  selectedPaymentIds: Set<number> = new Set();
  showBulkReminderModal = false;
  showAlternativePaymentModal = false;
  alternativePaymentTarget: PaymentItem | null = null;
  alternativePaymentForm = {
    method: 'BONIFICO' as 'BONIFICO' | 'SEDE',
    notes: ''
  };

  // --- Services Performed (prestazioni sanitarie) ---
  servicesPerformed: ServicePerformedItem[] = [];
  filteredServices: ServicePerformedItem[] = [];
  serviceStats: ServicePerformedStats = {
    totalServices: 0,
    paidWithin7Days: 0,
    paidWithReminder: 0,
    stillPending: 0,
    percentWithin7Days: 0,
    percentWithReminder: 0,
    percentPending: 0,
    filteredCount: 0
  };
  serviceStatusFilter: 'ALL' | 'PENDING' | 'PAID' | 'FREE' | 'CANCELLED' = 'ALL';
  serviceDaysFilter: number | null = null;
  selectedServiceIds = new Set<number>();
  servicesError = '';
  servicesSuccess = '';

  // Modali prestazioni
  showServiceEditModal = false;
  showServiceFreeModal = false;
  showServiceDetailModal = false;
  selectedService: ServicePerformedItem | null = null;
  selectedServiceDetail: ServicePerformedItem | null = null;
  serviceFreeReason = '';
  serviceEditForm = {
    amountCents: 0,
    amountEur: 0,
    patientName: '',
    patientEmail: '',
    notes: ''
  };

  // Modale nuova prestazione
  showAdminPaymentModal = false;
  paymentForm = {
    paymentId: null as number | null,
    receiptName: '',
    service: '',
    amount: 0,
    doctorId: null as number | null,
    patientId: null as number | null,
    performedAt: '',
    paymentType: 'VISITA' as PaymentTypeEnum
  };
  filteredPatientsForPayment: PatientItem[] = [];

  // Support data
  doctors: DoctorItem[] = [];
  patients: PatientItem[] = [];
  facilities: FacilityItem[] = [];
  departments: DepartmentItem[] = [];

  // Paginazione - due set separati
  pageSize = 5;
  pageSizeOptions = [5, 10, 20, 50];
  private pageState: Record<string, number> = {};

  constructor(private paymentsService: PaymentsService) {}

  ngOnInit(): void {
    this.loadPayments();
    this.loadServicesPerformed();
    this.loadDoctors();
    this.loadPatients();
    this.loadFacilities();
    this.loadDepartments();
  }

  // ========== PAGINAZIONE ==========

  getPage(key: string): number {
    return this.pageState[key] ?? 1;
  }

  getTotalPages(total: number): number {
    return Math.max(1, Math.ceil(total / this.pageSize));
  }

  setPage(key: string, page: number, total: number): void {
    const totalPages = this.getTotalPages(total);
    const nextPage = Math.min(Math.max(1, page), totalPages);
    this.pageState[key] = nextPage;
  }

  getPageSliceStart(key: string): number {
    return (this.getPage(key) - 1) * this.pageSize;
  }

  getPageSliceEnd(key: string): number {
    return this.getPage(key) * this.pageSize;
  }

  resetPagination(): void {
    this.pageState = {};
  }

  // ========== CARICAMENTO DATI ==========

  loadPayments(): void {
    this.isLoading = true;
    this.paymentsError = '';
    this.paymentsSuccess = '';

    this.paymentsService.loadPayments().subscribe({
      next: (payments) => {
        this.payments = payments;
        this.updatePaymentStats();
        this.isLoading = false;
      },
      error: () => {
        this.paymentsError = 'Impossibile caricare i pagamenti.';
        this.isLoading = false;
      }
    });
  }

  loadServicesPerformed(): void {
    this.isLoading = true;
    this.servicesError = '';
    this.servicesSuccess = '';

    // Carica statistiche
    this.paymentsService.loadServiceStats().subscribe({
      next: (stats) => {
        this.serviceStats = stats;
      },
      error: () => {}
    });

    // Carica lista prestazioni
    this.paymentsService.loadServicesPerformed(this.serviceStatusFilter).subscribe({
      next: (services) => {
        this.servicesPerformed = services;
        this.updateFilteredServices();
        this.isLoading = false;
      },
      error: () => {
        this.servicesError = 'Impossibile caricare le prestazioni.';
        this.isLoading = false;
      }
    });
  }

  // ========== SUPPORT LOADERS ==========

  private loadDoctors(): void {
    this.paymentsService.loadDoctors().subscribe({
      next: (doctors) => this.doctors = doctors,
      error: () => this.doctors = []
    });
  }

  private loadPatients(): void {
    this.paymentsService.loadPatients().subscribe({
      next: (patients) => this.patients = patients,
      error: () => this.patients = []
    });
  }

  private loadFacilities(): void {
    this.paymentsService.loadFacilities().subscribe({
      next: (facilities) => this.facilities = facilities,
      error: () => this.facilities = []
    });
  }

  private loadDepartments(): void {
    this.paymentsService.loadDepartments().subscribe({
      next: (departments) => this.departments = departments,
      error: () => this.departments = []
    });
  }

  // ========== STATISTICHE PAGAMENTI ==========

  private updatePaymentStats(): void {
    this.paymentStats = this.paymentsService.computePaymentStats(this.payments);
  }

  // ========== FILTRI PRESTAZIONI ==========

  private updateFilteredServices(): void {
    let filtered = [...this.servicesPerformed];

    if (this.serviceStatusFilter !== 'ALL') {
      filtered = filtered.filter(s => s.status === this.serviceStatusFilter);
    }

    if (this.serviceDaysFilter !== null) {
      const cutoffDate = new Date();
      cutoffDate.setDate(cutoffDate.getDate() - this.serviceDaysFilter);
      filtered = filtered.filter(s => new Date(s.performedAt) < cutoffDate);
    }

    this.filteredServices = filtered;

    if (this.serviceDaysFilter === null && this.serviceStatusFilter === 'ALL') {
      // Nessun filtro locale: usa il conteggio totale dal server
    } else if (this.serviceDaysFilter !== null) {
      this.serviceStats = { ...this.serviceStats, filteredCount: filtered.length };
    }
  }

  onServiceStatusFilterChange(): void {
    this.pageState['adminServices'] = 1;
    this.loadServicesPerformed();
  }

  onServiceDaysFilterChange(): void {
    this.pageState['adminServices'] = 1;
    this.updateFilteredServices();
  }

  // ========== FILTRI PAGAMENTI (PAYMENT ORDERS) ==========

  get filteredPayments(): PaymentItem[] {
    let result = this.payments;

    if (this.paymentStatusFilter !== 'ALL') {
      result = result.filter((p) => p.status === this.paymentStatusFilter);
    }

    if (this.paymentDaysFilter) {
      const now = new Date();
      result = result.filter((p) => {
        if (!p.createdAt) return false;
        const createdDate = new Date(p.createdAt);
        const diffDays = Math.floor((now.getTime() - createdDate.getTime()) / (1000 * 60 * 60 * 24));
        return diffDays > this.paymentDaysFilter!;
      });
    }

    return result;
  }

  getPaymentDaysOverdue(payment: PaymentItem): number {
    if (!payment.createdAt) return 0;
    const now = new Date();
    const createdDate = new Date(payment.createdAt);
    return Math.max(0, Math.floor((now.getTime() - createdDate.getTime()) / (1000 * 60 * 60 * 24)));
  }

  // ========== SELEZIONE PAGAMENTI ==========

  togglePaymentSelection(paymentId: number): void {
    if (this.selectedPaymentIds.has(paymentId)) {
      this.selectedPaymentIds.delete(paymentId);
    } else {
      this.selectedPaymentIds.add(paymentId);
    }
  }

  toggleAllPaymentsSelection(): void {
    const pendingPayments = this.filteredPayments.filter((p) => p.status === 'CREATED');
    if (this.selectedPaymentIds.size === pendingPayments.length) {
      this.selectedPaymentIds.clear();
    } else {
      pendingPayments.forEach((p) => this.selectedPaymentIds.add(p.id));
    }
  }

  isPaymentSelected(paymentId: number): boolean {
    return this.selectedPaymentIds.has(paymentId);
  }

  get selectedPaymentsCount(): number {
    return this.selectedPaymentIds.size;
  }

  get hasSelectedPayments(): boolean {
    return this.selectedPaymentIds.size > 0;
  }

  get areAllPaymentsSelected(): boolean {
    const pendingPayments = this.payments.filter((p) => p.status === 'CREATED');
    return pendingPayments.length > 0 && this.selectedPaymentIds.size === pendingPayments.length;
  }

  get paymentsCapturedCount(): number {
    return this.payments.filter((p) => p.status === 'CAPTURED').length;
  }

  get paymentsPendingCount(): number {
    return this.payments.filter((p) => p.status === 'CREATED').length;
  }

  get paymentsFailedCount(): number {
    return this.payments.filter((p) => p.status === 'FAILED' || p.status === 'CANCELLED').length;
  }

  // ========== SELEZIONE PRESTAZIONI ==========

  toggleServiceSelection(id: number): void {
    if (this.selectedServiceIds.has(id)) {
      this.selectedServiceIds.delete(id);
    } else {
      this.selectedServiceIds.add(id);
    }
  }

  isServiceSelected(id: number): boolean {
    return this.selectedServiceIds.has(id);
  }

  toggleAllServicesSelection(): void {
    const start = this.getPageSliceStart('adminServices');
    const end = start + this.pageSize;
    const visibleServices = this.filteredServices.slice(start, end);
    const visiblePending = visibleServices.filter(s => s.status === 'PENDING');
    const allVisibleSelected = visiblePending.every(s => this.selectedServiceIds.has(s.id));
    if (allVisibleSelected && visiblePending.length > 0) {
      visiblePending.forEach(s => this.selectedServiceIds.delete(s.id));
    } else {
      visiblePending.forEach(s => this.selectedServiceIds.add(s.id));
    }
  }

  // ========== AZIONI PAYMENT ORDERS ==========

  confirmAdminPayment(payment: PaymentItem): void {
    if (payment.status === 'CAPTURED') return;
    this.isLoading = true;
    this.paymentsError = '';
    this.paymentsService.capturePayment(payment.id).subscribe({
      next: (updated) => {
        this.payments = this.payments.map((p) => p.id === updated.id ? updated : p);
        this.updatePaymentStats();
        this.paymentsSuccess = `Pagamento #${payment.id} confermato con successo.`;
        this.isLoading = false;
      },
      error: () => {
        this.paymentsError = `Impossibile confermare il pagamento #${payment.id}.`;
        this.isLoading = false;
      }
    });
  }

  sendSingleReminder(payment: PaymentItem): void {
    if (payment.status === 'CAPTURED') return;
    this.isLoading = true;
    this.paymentsError = '';
    this.paymentsService.sendPaymentReminder(payment.id).subscribe({
      next: () => {
        this.paymentsSuccess = `Sollecito inviato a ${payment.patientName || payment.patientEmail || 'paziente'}.`;
        this.isLoading = false;
      },
      error: () => {
        this.paymentsError = `Impossibile inviare sollecito per il pagamento #${payment.id}.`;
        this.isLoading = false;
      }
    });
  }

  deleteAdminPayment(payment: PaymentItem): void {
    this.isLoading = true;
    this.paymentsError = '';
    this.paymentsService.cancelPayment(payment.id).subscribe({
      next: () => {
        this.payments = this.payments.filter((p) => p.id !== payment.id);
        this.selectedPaymentIds.delete(payment.id);
        this.updatePaymentStats();
        this.paymentsSuccess = `Pagamento #${payment.id} annullato.`;
        this.isLoading = false;
      },
      error: () => {
        this.paymentsError = `Impossibile annullare il pagamento #${payment.id}.`;
        this.isLoading = false;
      }
    });
  }

  confirmPayment(payment: PaymentItem): void {
    if (payment.status !== 'CREATED') return;
    this.confirmAdminPayment(payment);
  }

  // ========== BULK REMINDERS (PAYMENT ORDERS) ==========

  openBulkReminderModal(): void {
    if (this.selectedPaymentIds.size === 0) {
      this.paymentsError = 'Seleziona almeno un pagamento per inviare i solleciti.';
      return;
    }
    this.paymentsError = '';
    this.showBulkReminderModal = true;
  }

  closeBulkReminderModal(): void {
    this.showBulkReminderModal = false;
  }

  sendBulkReminders(): void {
    const paymentIds = Array.from(this.selectedPaymentIds);
    const count = paymentIds.length;
    if (count === 0) return;

    this.isLoading = true;
    this.paymentsError = '';
    let completed = 0;
    let errors = 0;

    paymentIds.forEach((id) => {
      this.paymentsService.sendPaymentReminder(id).subscribe({
        next: () => {
          completed++;
          this.payments = this.payments.map((p) =>
            p.id === id ? { ...p, notificationAttempts: [...(p.notificationAttempts || []), { sentAt: new Date().toISOString() }] } : p
          );
          if (completed + errors === count) this.finalizeBulkReminders(completed, errors);
        },
        error: () => {
          errors++;
          if (completed + errors === count) this.finalizeBulkReminders(completed, errors);
        }
      });
    });
  }

  private finalizeBulkReminders(completed: number, errors: number): void {
    this.isLoading = false;
    if (errors === 0) {
      this.paymentsSuccess = `Solleciti inviati con successo a ${completed} pazienti.`;
    } else if (completed === 0) {
      this.paymentsError = `Impossibile inviare i solleciti. Errori: ${errors}.`;
    } else {
      this.paymentsSuccess = `Solleciti inviati: ${completed}. Errori: ${errors}.`;
    }
    this.selectedPaymentIds.clear();
    this.closeBulkReminderModal();
  }

  // ========== ALTERNATIVE PAYMENT MODAL ==========

  openAlternativePaymentModal(payment: PaymentItem): void {
    this.alternativePaymentTarget = payment;
    this.alternativePaymentForm = { method: 'BONIFICO', notes: '' };
    this.paymentsError = '';
    this.showAlternativePaymentModal = true;
  }

  closeAlternativePaymentModal(): void {
    this.showAlternativePaymentModal = false;
    this.alternativePaymentTarget = null;
  }

  sendAlternativePaymentNotification(): void {
    if (!this.alternativePaymentTarget) return;

    const paymentId = this.alternativePaymentTarget.id;
    const methodLabel = this.alternativePaymentForm.method === 'BONIFICO' ? 'bonifico bancario' : 'pagamento in sede';

    this.isLoading = true;
    this.paymentsError = '';

    this.paymentsService.sendPaymentReminder(paymentId).subscribe({
      next: () => {
        this.payments = this.payments.map((p) =>
          p.id === paymentId ? { ...p, notificationAttempts: [...(p.notificationAttempts || []), { sentAt: new Date().toISOString() }] } : p
        );
        this.paymentsSuccess = `Sollecito con istruzioni per ${methodLabel} inviato al paziente.`;
        this.isLoading = false;
        this.closeAlternativePaymentModal();
      },
      error: () => {
        this.paymentsError = `Impossibile inviare il sollecito per il pagamento #${paymentId}.`;
        this.isLoading = false;
      }
    });
  }

  getNotificationAttemptsCount(payment: PaymentItem): number {
    return payment.notificationAttempts?.length || 0;
  }

  getLastNotificationDate(payment: PaymentItem): string {
    if (!payment.notificationAttempts?.length) return '-';
    const lastAttempt = payment.notificationAttempts[payment.notificationAttempts.length - 1];
    return this.formatDateTime(lastAttempt.sentAt);
  }

  // ========== AZIONI PRESTAZIONI ==========

  markServiceAsPaid(service: ServicePerformedItem): void {
    if (service.status === 'PAID') return;
    this.isLoading = true;
    this.servicesError = '';
    this.paymentsService.markServiceAsPaid(service.id).subscribe({
      next: (updated) => {
        this.servicesPerformed = this.servicesPerformed.map(s => s.id === updated.id ? updated : s);
        this.updateFilteredServices();
        this.servicesSuccess = `Prestazione #${service.id} segnata come pagata.`;
        this.isLoading = false;
        this.loadServicesPerformed();
      },
      error: () => {
        this.servicesError = `Impossibile confermare il pagamento della prestazione #${service.id}.`;
        this.isLoading = false;
      }
    });
  }

  openServiceFreeModal(service: ServicePerformedItem): void {
    this.selectedService = service;
    this.serviceFreeReason = '';
    this.showServiceFreeModal = true;
  }

  closeServiceFreeModal(): void {
    this.showServiceFreeModal = false;
    this.selectedService = null;
    this.serviceFreeReason = '';
    this.servicesError = '';
  }

  confirmMarkServiceAsFree(): void {
    if (!this.selectedService) return;
    this.isLoading = true;
    this.servicesError = '';
    this.paymentsService.markServiceAsFree(this.selectedService.id, this.serviceFreeReason || undefined).subscribe({
      next: (updated) => {
        this.servicesPerformed = this.servicesPerformed.map(s => s.id === updated.id ? updated : s);
        this.updateFilteredServices();
        this.servicesSuccess = `Prestazione #${this.selectedService!.id} convertita in gratuita.`;
        this.closeServiceFreeModal();
        this.isLoading = false;
        this.loadServicesPerformed();
      },
      error: () => {
        this.servicesError = `Impossibile convertire la prestazione #${this.selectedService!.id} in gratuita.`;
        this.isLoading = false;
      }
    });
  }

  openServiceEditModal(service: ServicePerformedItem): void {
    this.selectedService = service;
    this.serviceEditForm = {
      amountCents: service.amountCents,
      amountEur: service.amountCents ? service.amountCents / 100 : 0,
      patientName: service.patientName || '',
      patientEmail: service.patientEmail || '',
      notes: service.notes || ''
    };
    this.servicesError = '';
    this.showServiceEditModal = true;
  }

  closeServiceEditModal(): void {
    this.showServiceEditModal = false;
    this.selectedService = null;
    this.servicesError = '';
  }

  saveServiceEdit(): void {
    if (!this.selectedService) return;
    this.isLoading = true;
    this.servicesError = '';

    const payload: ServiceEditPayload = {
      amountCents: Math.round(this.serviceEditForm.amountEur * 100),
      patientName: this.serviceEditForm.patientName || null,
      patientEmail: this.serviceEditForm.patientEmail || null,
      notes: this.serviceEditForm.notes || null
    };

    this.paymentsService.updateService(this.selectedService.id, payload).subscribe({
      next: (updated) => {
        this.servicesPerformed = this.servicesPerformed.map(s => s.id === updated.id ? updated : s);
        this.updateFilteredServices();
        this.servicesSuccess = `Prestazione #${this.selectedService!.id} aggiornata.`;
        this.closeServiceEditModal();
        this.isLoading = false;
      },
      error: () => {
        this.servicesError = `Impossibile aggiornare la prestazione #${this.selectedService!.id}.`;
        this.isLoading = false;
      }
    });
  }

  deleteService(service: ServicePerformedItem): void {
    if (service.status === 'PAID') {
      this.servicesError = `Impossibile eliminare la prestazione #${service.id}: prestazione già pagata.`;
      return;
    }
    if (!confirm(`Sei sicuro di voler eliminare la prestazione #${service.id}?`)) return;
    this.isLoading = true;
    this.servicesError = '';
    this.paymentsService.deleteService(service.id).subscribe({
      next: () => {
        this.servicesPerformed = this.servicesPerformed.filter(s => s.id !== service.id);
        this.selectedServiceIds.delete(service.id);
        this.updateFilteredServices();
        this.servicesSuccess = `Prestazione #${service.id} eliminata.`;
        this.isLoading = false;
        this.loadServicesPerformed();
      },
      error: () => {
        this.servicesError = `Impossibile eliminare la prestazione #${service.id}.`;
        this.isLoading = false;
      }
    });
  }

  sendServiceReminder(service: ServicePerformedItem): void {
    if (service.status !== 'PENDING') return;
    this.isLoading = true;
    this.servicesError = '';
    this.paymentsService.sendServiceReminder(service.id).subscribe({
      next: (updated) => {
        this.servicesPerformed = this.servicesPerformed.map(s => s.id === updated.id ? updated : s);
        this.updateFilteredServices();
        this.servicesSuccess = `Sollecito inviato per prestazione #${service.id}.`;
        this.isLoading = false;
      },
      error: () => {
        this.servicesError = `Impossibile inviare sollecito per la prestazione #${service.id}. Verifica che l'email del paziente sia presente.`;
        this.isLoading = false;
      }
    });
  }

  sendBulkServiceReminders(): void {
    if (this.selectedServiceIds.size === 0) return;
    this.isLoading = true;
    this.servicesError = '';
    const ids = Array.from(this.selectedServiceIds);
    this.paymentsService.sendBulkServiceReminders(ids).subscribe({
      next: (result) => {
        const sent = result?.sent ?? 0;
        const skipped = result?.skipped ?? 0;
        if (sent > 0 && skipped > 0) {
          this.servicesSuccess = `Solleciti inviati: ${sent} inviati, ${skipped} saltati (stato non valido o email mancante).`;
        } else if (sent > 0) {
          this.servicesSuccess = `Solleciti inviati con successo a ${sent} pazienti.`;
        } else {
          this.servicesSuccess = `Nessun sollecito inviato: ${skipped} prestazioni saltate (stato non valido o email mancante).`;
        }
        this.selectedServiceIds.clear();
        this.isLoading = false;
        this.loadServicesPerformed();
      },
      error: () => {
        this.servicesError = 'Impossibile inviare i solleciti.';
        this.isLoading = false;
      }
    });
  }

  openServiceDetailModal(service: ServicePerformedItem): void {
    this.selectedServiceDetail = service;
    this.showServiceDetailModal = true;
  }

  closeServiceDetailModal(): void {
    this.showServiceDetailModal = false;
    this.selectedServiceDetail = null;
  }

  // ========== MODALE NUOVA PRESTAZIONE ==========

  openAdminPaymentModal(): void {
    this.paymentsError = '';
    this.paymentsSuccess = '';
    this.paymentForm.doctorId = null;
    this.paymentForm.patientId = null;
    this.paymentForm.service = '';
    this.paymentForm.amount = 0;
    this.paymentForm.performedAt = '';
    this.paymentForm.paymentType = 'VISITA';
    this.filteredPatientsForPayment = [];
    this.showAdminPaymentModal = true;
  }

  onPaymentDoctorChange(): void {
    this.paymentForm.patientId = null;
    this.filteredPatientsForPayment = [];
    if (!this.paymentForm.doctorId) {
      return;
    }
    this.paymentsService.loadPatientsWithConsent(this.paymentForm.doctorId).subscribe({
      next: (patientIds) => {
        this.filteredPatientsForPayment = this.patients.filter(p => patientIds.includes(p.id));
      },
      error: () => {
        this.paymentsError = 'Impossibile caricare i pazienti per questo medico.';
      }
    });
  }

  closeAdminPaymentModal(): void {
    this.showAdminPaymentModal = false;
    this.paymentsError = '';
  }

  submitNewPayment(): void {
    const errors: string[] = [];
    if (!this.paymentForm.doctorId) {
      errors.push('Seleziona un medico.');
    }
    if (!this.paymentForm.patientId) {
      errors.push('Seleziona un paziente.');
    }
    if (!this.paymentForm.service.trim()) {
      errors.push('Inserisci la descrizione della prestazione.');
    }
    if (!this.paymentForm.amount || this.paymentForm.amount <= 0) {
      errors.push("Inserisci l'importo del pagamento.");
    }
    if (!this.paymentForm.performedAt) {
      errors.push('Inserisci la data della prestazione.');
    }
    if (errors.length > 0) {
      this.paymentsError = errors.join(' ');
      return;
    }
    this.isLoading = true;
    this.paymentsError = '';
    this.paymentsSuccess = '';

    const payload: ServiceCreatePayload = {
      doctorId: this.paymentForm.doctorId!,
      patientId: this.paymentForm.patientId!,
      paymentType: this.paymentForm.paymentType,
      description: this.paymentForm.service,
      amountCents: Math.round(this.paymentForm.amount * 100),
      performedAt: new Date(this.paymentForm.performedAt).toISOString()
    };

    this.paymentsService.createService(payload).subscribe({
      next: (service) => {
        this.servicesPerformed = [service, ...this.servicesPerformed];
        this.updateFilteredServices();
        this.paymentsSuccess = `Pagamento #${service.id} creato con successo.`;
        this.paymentForm.doctorId = null;
        this.paymentForm.patientId = null;
        this.paymentForm.service = '';
        this.paymentForm.amount = 0;
        this.paymentForm.performedAt = '';
        this.paymentForm.paymentType = 'VISITA';
        this.filteredPatientsForPayment = [];
        this.isLoading = false;
        setTimeout(() => {
          this.closeAdminPaymentModal();
          this.paymentsSuccess = '';
        }, 1500);
      },
      error: () => {
        this.paymentsError = 'Impossibile creare il pagamento.';
        this.isLoading = false;
      }
    });
  }

  // ========== LABEL HELPERS ==========

  getPaymentStatusLabel(payment: PaymentItem): string {
    const labels: Record<string, string> = {
      CREATED: 'In attesa di pagamento',
      CAPTURED: 'Pagamento ricevuto',
      FAILED: 'Non riuscito',
      CANCELLED: 'Annullato',
      REFUNDED: 'Rimborsato'
    };
    return labels[payment.status] ?? payment.status;
  }

  getPaymentMethodLabel(method: string): string {
    const labels: Record<string, string> = {
      CARD: 'Carta',
      BANK_TRANSFER: 'Bonifico',
      CASH: 'Contanti'
    };
    return labels[method] ?? method;
  }

  getServiceTypeLabel(type: string): string {
    switch (type) {
      case 'MEDICAL_VISIT': return 'Visita medica';
      case 'HOSPITALIZATION': return 'Ricovero';
      default: return type;
    }
  }

  getPaymentTypeLabel(type: string | undefined): string {
    switch (type) {
      case 'VISITA': return 'Visita';
      case 'RICOVERO': return 'Ricovero';
      case 'ALTRO': return 'Altro';
      default: return type || '-';
    }
  }

  getServiceStatusLabel(status: string): string {
    switch (status) {
      case 'PENDING': return 'In attesa';
      case 'PAID': return 'Pagato';
      case 'FREE': return 'Gratuito';
      case 'CANCELLED': return 'Annullato';
      default: return status;
    }
  }

  getServiceDaysOverdue(service: ServicePerformedItem): number {
    if (!service.performedAt) return 0;
    if (service.status === 'PAID' || service.status === 'FREE' || service.status === 'CANCELLED') return -1;
    const performedDate = new Date(service.performedAt);
    const today = new Date();
    const diffTime = today.getTime() - performedDate.getTime();
    return Math.floor(diffTime / (1000 * 60 * 60 * 24));
  }

  getDoctorLabel(doctorId: number | null | undefined): string {
    if (doctorId == null) return '-';
    const doctor = this.doctors.find((item) => item.id === doctorId);
    return doctor ? `${doctor.firstName} ${doctor.lastName}` : `Medico ${doctorId}`;
  }

  getPatientLabel(patientId: number): string {
    const patient = this.patients.find((item) => item.id === patientId);
    return patient ? `${patient.firstName} ${patient.lastName}` : `Paziente ${patientId}`;
  }

  getDepartmentNameByCode(code: string | undefined): string {
    if (!code) return '-';
    const dept = this.departments.find(d => d.code === code);
    return dept?.name ?? code;
  }

  getFacilityNameByDepartmentCode(code: string | undefined): string {
    if (!code) return '-';
    const dept = this.departments.find(d => d.code === code);
    if (!dept?.facilityCode) return '-';
    const facility = this.facilities.find(f => f.code === dept.facilityCode);
    return facility?.name ?? dept.facilityCode;
  }

  canMarkPaymentAsPaid(payment: PaymentItem): boolean {
    return payment.status === 'CREATED';
  }

  // ========== FORMAT HELPERS ==========

  formatDate(value: string): string {
    if (!value) return '-';
    const normalizedValue = value.trim();
    if (/^\d{2}\/\d{2}\/\d{4}$/.test(normalizedValue)) {
      return normalizedValue;
    }
    const isoMatch = normalizedValue.match(/^(\d{4})-(\d{2})-(\d{2})/);
    if (isoMatch) {
      return `${isoMatch[3]}/${isoMatch[2]}/${isoMatch[1]}`;
    }
    const parsed = new Date(normalizedValue);
    if (Number.isNaN(parsed.getTime())) {
      return normalizedValue;
    }
    const day = String(parsed.getDate()).padStart(2, '0');
    const month = String(parsed.getMonth() + 1).padStart(2, '0');
    return `${day}/${month}/${parsed.getFullYear()}`;
  }

  formatDateTime(value: string): string {
    if (!value) return '-';
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return value;
    }
    const day = String(parsed.getDate()).padStart(2, '0');
    const month = String(parsed.getMonth() + 1).padStart(2, '0');
    const year = parsed.getFullYear();
    const hours = String(parsed.getHours()).padStart(2, '0');
    const minutes = String(parsed.getMinutes()).padStart(2, '0');
    return `${day}/${month}/${year} ${hours}:${minutes}`;
  }
}
