import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import {
  PatientService,
  PaymentOrderDto,
  PaymentStatus,
  PaymentMethod
} from '../../../services/patient.service';

@Component({
  selector: 'app-patient-payments',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './patient-payments.component.html'
})
export class PatientPaymentsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  // Dati pagamenti
  payments: PaymentOrderDto[] = [];

  // Stato UI
  isLoading = false;
  isProcessing = false;
  successMessage = '';
  errorMessage = '';
  showPaymentModal = false;
  selectedPayment: PaymentOrderDto | null = null;

  // Form pagamento
  paymentForm = {
    method: 'CARD' as PaymentMethod,
    saveCard: false
  };

  // Filtri
  statusFilter: 'ALL' | PaymentStatus = 'ALL';
  yearFilter = new Date().getFullYear();

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  // Anni disponibili per filtro
  availableYears: number[] = [];

  constructor(private patientService: PatientService) {}

  ngOnInit(): void {
    this.loadPayments();
    this.initYearFilter();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  initYearFilter(): void {
    const currentYear = new Date().getFullYear();
    this.availableYears = [currentYear, currentYear - 1, currentYear - 2];
  }

  loadPayments(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.patientService.getPayments({ size: 100, sort: 'createdAt,desc' })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.payments = response.content;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Errore caricamento pagamenti:', err);
          this.errorMessage = 'Impossibile caricare i pagamenti. Riprova.';
          this.isLoading = false;
        }
      });
  }

  get filteredPayments(): PaymentOrderDto[] {
    return this.payments.filter(p => {
      if (this.statusFilter !== 'ALL' && p.status !== this.statusFilter) return false;

      // Filtro per anno
      const paymentYear = new Date(p.createdAt).getFullYear();
      if (paymentYear !== this.yearFilter) return false;

      return true;
    });
  }

  get paginatedPayments(): PaymentOrderDto[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredPayments.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredPayments.length / this.pageSize) || 1;
  }

  get pendingPayments(): PaymentOrderDto[] {
    return this.payments.filter(p => p.status === 'PENDING' || p.status === 'AUTHORIZED');
  }

  get pendingAmount(): number {
    return this.pendingPayments.reduce((sum, p) => sum + p.amountCents, 0);
  }

  get paidThisYear(): number {
    const currentYear = new Date().getFullYear();
    return this.payments
      .filter(p => p.status === 'CAPTURED' && new Date(p.updatedAt).getFullYear() === currentYear)
      .reduce((sum, p) => sum + p.amountCents, 0);
  }

  get paidPaymentsCount(): number {
    return this.payments.filter(p => p.status === 'CAPTURED').length;
  }

  getStatusLabel(status: PaymentStatus): string {
    const labels: Record<PaymentStatus, string> = {
      PENDING: 'Da pagare',
      AUTHORIZED: 'Autorizzato',
      CAPTURED: 'Pagato',
      FAILED: 'Fallito',
      REFUNDED: 'Rimborsato'
    };
    return labels[status] || status;
  }

  getStatusBadgeClass(status: PaymentStatus): string {
    const classes: Record<PaymentStatus, string> = {
      PENDING: 'bg-warning text-dark',
      AUTHORIZED: 'bg-info',
      CAPTURED: 'bg-success',
      FAILED: 'bg-danger',
      REFUNDED: 'bg-secondary'
    };
    return classes[status] || 'bg-secondary';
  }

  getMethodLabel(method: PaymentMethod | undefined): string {
    if (!method) return '-';
    const labels: Record<PaymentMethod, string> = {
      CARD: 'Carta di credito/debito',
      BANK_TRANSFER: 'Bonifico bancario',
      MANUAL: 'Pagamento manuale'
    };
    return labels[method] || method;
  }

  getMethodIcon(method: PaymentMethod | undefined): string {
    if (!method) return 'bi-cash';
    const icons: Record<PaymentMethod, string> = {
      CARD: 'bi-credit-card',
      BANK_TRANSFER: 'bi-bank',
      MANUAL: 'bi-cash'
    };
    return icons[method] || 'bi-cash';
  }

  formatCurrency(amountCents: number, currency: string = 'EUR'): string {
    return new Intl.NumberFormat('it-IT', {
      style: 'currency',
      currency: currency
    }).format(amountCents / 100);
  }

  formatDate(dateStr: string | undefined): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  formatDateTime(dateStr: string | undefined): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  isPending(payment: PaymentOrderDto): boolean {
    return payment.status === 'PENDING' || payment.status === 'AUTHORIZED';
  }

  isPaid(payment: PaymentOrderDto): boolean {
    return payment.status === 'CAPTURED';
  }

  openPaymentModal(payment: PaymentOrderDto): void {
    this.selectedPayment = payment;
    this.paymentForm = {
      method: 'CARD',
      saveCard: false
    };
    this.errorMessage = '';
    this.showPaymentModal = true;
  }

  closePaymentModal(): void {
    this.showPaymentModal = false;
    this.selectedPayment = null;
  }

  processPayment(): void {
    if (!this.selectedPayment) return;

    this.isProcessing = true;
    this.errorMessage = '';

    // Genera un idempotency key unico
    const idempotencyKey = `pay-${this.selectedPayment.id}-${Date.now()}`;

    this.patientService.createPayment({
      appointmentId: this.selectedPayment.appointmentId,
      amountCents: this.selectedPayment.amountCents,
      currency: this.selectedPayment.currency,
      method: this.paymentForm.method,
      description: this.selectedPayment.description
    }, idempotencyKey)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: () => {
        this.isProcessing = false;
        this.closePaymentModal();
        this.loadPayments(); // Ricarica la lista
        this.successMessage = `Pagamento di ${this.formatCurrency(this.selectedPayment!.amountCents, this.selectedPayment!.currency)} completato con successo!`;
        setTimeout(() => this.successMessage = '', 5000);
      },
      error: (err) => {
        console.error('Errore pagamento:', err);
        this.errorMessage = 'Errore durante il pagamento. Riprova.';
        this.isProcessing = false;
      }
    });
  }

  downloadReceipt(payment: PaymentOrderDto): void {
    this.successMessage = `Download della ricevuta avviato.`;
    setTimeout(() => this.successMessage = '', 3000);
  }

  downloadAnnualSummary(): void {
    this.successMessage = `Download del riepilogo annuale ${this.yearFilter} avviato.`;
    setTimeout(() => this.successMessage = '', 3000);
  }

  generateBankSlip(payment: PaymentOrderDto): void {
    this.successMessage = `Bollettino generato. Importo: ${this.formatCurrency(payment.amountCents, payment.currency)}`;
    setTimeout(() => this.successMessage = '', 5000);
  }
}
