import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuditService } from './audit.service';
import { AuditEventDetail } from './dtos/audit.dto';

@Component({
  selector: 'app-admin-audit',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-audit.component.html'
})
export class AdminAuditComponent implements OnInit {

  // Stato
  isLoading = false;
  auditError = '';
  auditDetailedEvents: AuditEventDetail[] = [];
  auditFilteredEvents: AuditEventDetail[] = [];
  auditSearchText = '';
  auditSelectedEvent: AuditEventDetail | null = null;

  // Paginazione
  pageSize = 10;
  pageSizeOptions = [5, 10, 20, 50];
  currentPage = 1;

  // Opzioni esito
  auditOutcomeOptions = [
    { value: 'SUCCESS', label: 'Successo' },
    { value: 'DENIED', label: 'Negato' },
    { value: 'FAILURE', label: 'Fallito' }
  ];

  constructor(private auditService: AuditService) {}

  ngOnInit(): void {
    this.loadAllAuditEvents();
  }

  // — Caricamento dati —

  loadAllAuditEvents(): void {
    this.isLoading = true;
    this.auditError = '';
    this.auditDetailedEvents = [];
    this.auditFilteredEvents = [];
    this.auditSearchText = '';
    this.auditSelectedEvent = null;

    this.auditService.loadAllAuditEvents().subscribe({
      next: (events) => {
        this.auditDetailedEvents = events;
        this.filterAuditByText();
        this.isLoading = false;
      },
      error: (err) => {
        if (err.status === 403) {
          this.auditError = 'Non hai i permessi per accedere ai dati di audit.';
        } else {
          this.auditError = 'Errore durante il caricamento degli eventi audit.';
        }
        this.isLoading = false;
      }
    });
  }

  // — Filtro testo —

  filterAuditByText(): void {
    if (!this.auditSearchText.trim()) {
      this.auditFilteredEvents = [...this.auditDetailedEvents];
    } else {
      const searchLower = this.auditSearchText.toLowerCase();
      this.auditFilteredEvents = this.auditDetailedEvents.filter(e =>
        e.action.toLowerCase().includes(searchLower) ||
        e.actorId.toLowerCase().includes(searchLower) ||
        e.actorName.toLowerCase().includes(searchLower) ||
        (e.actorType?.toLowerCase().includes(searchLower)) ||
        (e.resourceType?.toLowerCase().includes(searchLower)) ||
        (e.resourceId?.toLowerCase().includes(searchLower)) ||
        e.outcome.toLowerCase().includes(searchLower) ||
        (e.serviceName?.toLowerCase().includes(searchLower))
      );
    }
    this.currentPage = 1;
  }

  // — Modale dettaglio —

  selectAuditEvent(event: AuditEventDetail): void {
    this.auditSelectedEvent = event;
    setTimeout(() => {
      const modal = document.querySelector('.modal[tabindex="-1"]') as HTMLElement;
      if (modal) modal.focus();
    }, 50);
  }

  closeAuditEventDetail(): void {
    this.auditSelectedEvent = null;
  }

  // — Helpers UI —

  getOutcomeLabel(outcome: string): string {
    return this.auditOutcomeOptions.find(o => o.value === outcome)?.label ?? outcome;
  }

  getOutcomeBadgeClass(outcome: string): string {
    switch (outcome) {
      case 'SUCCESS': return 'bg-success';
      case 'DENIED': return 'bg-danger';
      case 'FAILURE': return 'bg-warning text-dark';
      default: return 'bg-secondary';
    }
  }

  formatAuditDate(isoDate: string): string {
    return new Date(isoDate).toLocaleDateString('it-IT', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit', second: '2-digit'
    });
  }

  // — Paginazione —

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.auditFilteredEvents.length / this.pageSize));
  }

  get sliceStart(): number {
    return (this.currentPage - 1) * this.pageSize;
  }

  get sliceEnd(): number {
    return this.currentPage * this.pageSize;
  }

  setPage(page: number): void {
    this.currentPage = Math.min(Math.max(1, page), this.totalPages);
  }

  onPageSizeChange(): void {
    this.currentPage = 1;
  }
}
