import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

type PaymentStatus = 'PENDING' | 'PAID' | 'OVERDUE' | 'CANCELLED';
type PaymentMethod = 'CARD' | 'PAYPAL' | 'BANK_TRANSFER';

interface Payment {
  id: number;
  description: string;
  service: string;
  amount: number;
  dueDate?: string;
  paidAt?: string;
  status: PaymentStatus;
  method?: PaymentMethod;
  receiptUrl?: string;
  visitDate: string;
}

@Component({
  selector: 'app-patient-payments',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './patient-payments.component.html'
})
export class PatientPaymentsComponent implements OnInit {
  // Dati pagamenti
  payments: Payment[] = [];

  // UI State
  isLoading = false;
  isProcessing = false;
  successMessage = '';
  errorMessage = '';
  showPaymentModal = false;
  selectedPayment: Payment | null = null;

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

  ngOnInit(): void {
    this.loadPayments();
    this.initYearFilter();
  }

  initYearFilter(): void {
    const currentYear = new Date().getFullYear();
    this.availableYears = [currentYear, currentYear - 1, currentYear - 2];
  }

  loadPayments(): void {
    this.isLoading = true;

    // Dati mock - Scenario di Sara
    setTimeout(() => {
      this.payments = [
        {
          id: 1,
          description: 'Ticket visita specialistica',
          service: 'Visita cardiologica',
          amount: 36.15,
          dueDate: '2025-02-15',
          status: 'PENDING',
          visitDate: '2025-01-20'
        },
        {
          id: 2,
          description: 'Ticket esami di laboratorio',
          service: 'Emocromo completo + Profilo lipidico',
          amount: 22.50,
          dueDate: '2025-02-01',
          status: 'OVERDUE',
          visitDate: '2025-01-10'
        },
        {
          id: 3,
          description: 'Ticket visita specialistica',
          service: 'Visita dermatologica',
          amount: 36.15,
          paidAt: '2025-01-05T10:30:00',
          status: 'PAID',
          method: 'CARD',
          receiptUrl: '/receipts/2025/003.pdf',
          visitDate: '2024-12-20'
        },
        {
          id: 4,
          description: 'Ticket esami diagnostici',
          service: 'ECG + Ecocardiogramma',
          amount: 45.00,
          paidAt: '2024-12-15T14:20:00',
          status: 'PAID',
          method: 'PAYPAL',
          receiptUrl: '/receipts/2024/045.pdf',
          visitDate: '2024-12-10'
        },
        {
          id: 5,
          description: 'Ticket visita specialistica',
          service: 'Visita ortopedica',
          amount: 36.15,
          paidAt: '2024-11-20T09:15:00',
          status: 'PAID',
          method: 'CARD',
          receiptUrl: '/receipts/2024/032.pdf',
          visitDate: '2024-11-15'
        },
        {
          id: 6,
          description: 'Ticket esami di laboratorio',
          service: 'Analisi del sangue complete',
          amount: 28.00,
          paidAt: '2024-10-05T11:00:00',
          status: 'PAID',
          method: 'BANK_TRANSFER',
          receiptUrl: '/receipts/2024/028.pdf',
          visitDate: '2024-09-28'
        },
        {
          id: 7,
          description: 'Ticket visita specialistica',
          service: 'Visita neurologica',
          amount: 36.15,
          status: 'CANCELLED',
          visitDate: '2024-09-15'
        }
      ];
      this.isLoading = false;
    }, 500);
  }

  get filteredPayments(): Payment[] {
    return this.payments.filter(p => {
      if (this.statusFilter !== 'ALL' && p.status !== this.statusFilter) return false;

      // Filtro per anno
      const paymentYear = new Date(p.visitDate).getFullYear();
      if (paymentYear !== this.yearFilter) return false;

      return true;
    });
  }

  get paginatedPayments(): Payment[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredPayments.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredPayments.length / this.pageSize) || 1;
  }

  get pendingPayments(): Payment[] {
    return this.payments.filter(p => p.status === 'PENDING' || p.status === 'OVERDUE');
  }

  get pendingAmount(): number {
    return this.pendingPayments.reduce((sum, p) => sum + p.amount, 0);
  }

  get paidThisYear(): number {
    const currentYear = new Date().getFullYear();
    return this.payments
      .filter(p => p.status === 'PAID' && p.paidAt && new Date(p.paidAt).getFullYear() === currentYear)
      .reduce((sum, p) => sum + p.amount, 0);
  }

  get paidPaymentsCount(): number {
    return this.payments.filter(p => p.status === 'PAID').length;
  }

  getStatusLabel(status: PaymentStatus): string {
    const labels: Record<PaymentStatus, string> = {
      PENDING: 'Da pagare',
      PAID: 'Pagato',
      OVERDUE: 'Scaduto',
      CANCELLED: 'Annullato'
    };
    return labels[status];
  }

  getStatusBadgeClass(status: PaymentStatus): string {
    const classes: Record<PaymentStatus, string> = {
      PENDING: 'bg-warning text-dark',
      PAID: 'bg-success',
      OVERDUE: 'bg-danger',
      CANCELLED: 'bg-secondary'
    };
    return classes[status];
  }

  getMethodLabel(method: PaymentMethod): string {
    const labels: Record<PaymentMethod, string> = {
      CARD: 'Carta di credito/debito',
      PAYPAL: 'PayPal',
      BANK_TRANSFER: 'Bonifico bancario'
    };
    return labels[method];
  }

  getMethodIcon(method: PaymentMethod): string {
    const icons: Record<PaymentMethod, string> = {
      CARD: 'bi-credit-card',
      PAYPAL: 'bi-paypal',
      BANK_TRANSFER: 'bi-bank'
    };
    return icons[method];
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('it-IT', {
      style: 'currency',
      currency: 'EUR'
    }).format(amount);
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  formatDateTime(dateStr: string): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  isOverdue(payment: Payment): boolean {
    if (payment.status !== 'PENDING' || !payment.dueDate) return false;
    return new Date(payment.dueDate) < new Date();
  }

  openPaymentModal(payment: Payment): void {
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

    // Simula elaborazione pagamento
    setTimeout(() => {
      const payment = this.selectedPayment!;
      payment.status = 'PAID';
      payment.paidAt = new Date().toISOString();
      payment.method = this.paymentForm.method;
      payment.receiptUrl = `/receipts/${new Date().getFullYear()}/${payment.id.toString().padStart(3, '0')}.pdf`;

      this.isProcessing = false;
      this.closePaymentModal();
      this.successMessage = `Pagamento di ${this.formatCurrency(payment.amount)} completato con successo! Ricevuta disponibile.`;
      setTimeout(() => this.successMessage = '', 5000);
    }, 2000);
  }

  downloadReceipt(payment: Payment): void {
    this.successMessage = `Download della ricevuta per "${payment.service}" avviato.`;
    setTimeout(() => this.successMessage = '', 3000);
  }

  downloadAnnualSummary(): void {
    this.successMessage = `Download del riepilogo annuale ${this.yearFilter} avviato.`;
    setTimeout(() => this.successMessage = '', 3000);
  }

  generateBankSlip(payment: Payment): void {
    this.successMessage = `Bollettino per "${payment.service}" generato. Importo: ${this.formatCurrency(payment.amount)}`;
    setTimeout(() => this.successMessage = '', 5000);
  }
}
