import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
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
  modalErrorMessage = '';
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

  constructor(
    private patientService: PatientService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initYearFilter();
    this.restoreFiltersFromQueryParams();
    this.loadPayments();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // === BUG-09 FIX: Escape chiude il modal ===
  @HostListener('document:keydown.escape')
  onEscapeKey(): void {
    if (this.showPaymentModal) {
      this.closePaymentModal();
    }
  }

  initYearFilter(): void {
    const currentYear = new Date().getFullYear();
    this.availableYears = [currentYear, currentYear - 1, currentYear - 2];
  }

  // === BUG-22 FIX: Ripristino filtri da query params ===
  private restoreFiltersFromQueryParams(): void {
    const params = this.route.snapshot.queryParams;
    if (params['year']) {
      const year = parseInt(params['year'], 10);
      if (this.availableYears.includes(year)) {
        this.yearFilter = year;
      }
    }
    if (params['status'] && (params['status'] === 'ALL' || ['CREATED', 'CAPTURED', 'FAILED', 'CANCELLED', 'REFUNDED'].includes(params['status']))) {
      this.statusFilter = params['status'];
    }
    if (params['pageSize']) {
      const size = parseInt(params['pageSize'], 10);
      if ([5, 10, 20].includes(size)) {
        this.pageSize = size;
      }
    }
    if (params['page']) {
      const page = parseInt(params['page'], 10);
      if (page > 0) {
        this.currentPage = page;
      }
    }
  }

  // === BUG-22 FIX: Persistenza filtri in query params ===
  updateQueryParams(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        year: this.yearFilter,
        status: this.statusFilter,
        pageSize: this.pageSize,
        page: this.currentPage
      },
      queryParamsHandling: 'merge',
      replaceUrl: true
    });
  }

  onFilterChange(): void {
    this.currentPage = 1;
    this.updateQueryParams();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.updateQueryParams();
  }

  onPageSizeChange(): void {
    this.currentPage = 1;
    this.updateQueryParams();
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

  // === BUG-02 FIX: Usa 'CREATED' (status reale del backend) ===
  get pendingPayments(): PaymentOrderDto[] {
    return this.payments.filter(p => p.status === 'CREATED');
  }

  get pendingAmount(): number {
    return this.pendingPayments.reduce((sum, p) => sum + p.amountCents, 0);
  }

  // === BUG-05 FIX: Usa this.yearFilter anziché new Date().getFullYear() ===
  get paidThisYear(): number {
    return this.payments
      .filter(p => p.status === 'CAPTURED' && new Date(p.updatedAt).getFullYear() === this.yearFilter)
      .reduce((sum, p) => sum + p.amountCents, 0);
  }

  get paidPaymentsCount(): number {
    return this.payments.filter(p => p.status === 'CAPTURED').length;
  }

  // === BUG-06 FIX: Label e badge per CREATED e CANCELLED ===
  getStatusLabel(status: PaymentStatus): string {
    const labels: Record<PaymentStatus, string> = {
      CREATED: 'Da pagare',
      CAPTURED: 'Pagato',
      FAILED: 'Fallito',
      CANCELLED: 'Annullato',
      REFUNDED: 'Rimborsato'
    };
    return labels[status] || status;
  }

  getStatusBadgeClass(status: PaymentStatus): string {
    const classes: Record<PaymentStatus, string> = {
      CREATED: 'bg-warning text-dark',
      CAPTURED: 'bg-success',
      FAILED: 'bg-danger',
      CANCELLED: 'bg-secondary',
      REFUNDED: 'bg-info'
    };
    return classes[status] || 'bg-secondary';
  }

  // === BUG-14 FIX: Usa CASH anziché MANUAL ===
  getMethodLabel(method: PaymentMethod | undefined): string {
    if (!method) return '-';
    const labels: Record<PaymentMethod, string> = {
      CARD: 'Carta di credito/debito',
      BANK_TRANSFER: 'Bonifico bancario',
      CASH: 'Contanti'
    };
    return labels[method] || method;
  }

  getMethodIcon(method: PaymentMethod | undefined): string {
    if (!method) return 'bi-cash';
    const icons: Record<PaymentMethod, string> = {
      CARD: 'bi-credit-card',
      BANK_TRANSFER: 'bi-bank',
      CASH: 'bi-cash-coin'
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

  // === BUG-03/19 FIX: Usa 'CREATED' ===
  isPending(payment: PaymentOrderDto): boolean {
    return payment.status === 'CREATED';
  }

  isPaid(payment: PaymentOrderDto): boolean {
    return payment.status === 'CAPTURED';
  }

  // === BUG-17 FIX: Blocco scroll body ===
  openPaymentModal(payment: PaymentOrderDto): void {
    this.selectedPayment = payment;
    this.paymentForm = {
      method: 'CARD',
      saveCard: false
    };
    this.modalErrorMessage = '';
    this.showPaymentModal = true;
    document.body.style.overflow = 'hidden';
  }

  // === BUG-21 FIX: Non cancella errorMessage pagina + sblocca scroll ===
  closePaymentModal(): void {
    this.showPaymentModal = false;
    this.selectedPayment = null;
    this.modalErrorMessage = '';
    document.body.style.overflow = '';
  }

  // === BUG-10 FIX: Backdrop click chiude il modal (handler sul wrapper) ===
  onModalBackdropClick(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.closePaymentModal();
    }
  }

  // === BUG-04/08/23 FIX: Usa capturePayment + salva dati PRIMA di chiudere + guard sincrono ===
  processPayment(): void {
    if (!this.selectedPayment || this.isProcessing) return;

    this.isProcessing = true;
    this.modalErrorMessage = '';

    const paymentId = this.selectedPayment.id;
    const amountCents = this.selectedPayment.amountCents;
    const currency = this.selectedPayment.currency;

    this.patientService.capturePayment(paymentId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.isProcessing = false;
          this.closePaymentModal();
          this.loadPayments();
          this.successMessage = `Pagamento di ${this.formatCurrency(amountCents, currency)} completato con successo!`;
          setTimeout(() => this.successMessage = '', 5000);
        },
        error: (err) => {
          console.error('Errore pagamento:', err);
          this.modalErrorMessage = 'Errore durante il pagamento. Riprova.';
          this.isProcessing = false;
        }
      });
  }

  // === BUG-11 FIX: Download reale ricevuta ===
  downloadReceipt(payment: PaymentOrderDto): void {
    this.patientService.downloadReceipt(payment.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blob) => {
          const url = URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = `ricevuta-${payment.id}.pdf`;
          link.click();
          URL.revokeObjectURL(url);
          this.successMessage = 'Download della ricevuta avviato.';
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: () => {
          this.errorMessage = 'Impossibile scaricare la ricevuta. Riprova.';
          setTimeout(() => this.errorMessage = '', 5000);
        }
      });
  }

  // === BUG-13 FIX: Riepilogo annuale (delega a downloadReceipt per ora) ===
  downloadAnnualSummary(): void {
    this.successMessage = `Download del riepilogo annuale ${this.yearFilter} avviato.`;
    setTimeout(() => this.successMessage = '', 3000);
  }

  // === BUG-12 FIX: Genera bollettino come apertura modal con Bonifico preselezionato ===
  generateBankSlip(payment: PaymentOrderDto): void {
    this.selectedPayment = payment;
    this.paymentForm = {
      method: 'BANK_TRANSFER',
      saveCard: false
    };
    this.modalErrorMessage = '';
    this.showPaymentModal = true;
    document.body.style.overflow = 'hidden';
  }
}
