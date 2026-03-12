import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { PaymentService } from '../../services/payment.service';

@Component({
  selector: 'app-parent-payments',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './parent-payments.component.html',
  styleUrl: './parent-payments.component.scss'
})
export class ParentPaymentsComponent implements OnInit {
  payments: any[] = [];        // unpaid
  paidPayments: any[] = [];    // paid
  paymentHistory: any[] = [];
  isLoading = true;
  showPaymentModal = false;
  selectedPayment: any = null;
  cardNumber = '';
  expiryDate = '';
  cvv = '';
  cardholderName = '';
  isProcessing = false;
  paymentError = '';
  activeTab = 'pending';

  constructor(private authService: AuthService, private paymentService: PaymentService) {}

  ngOnInit() {
    const user = this.authService.currentUser;
    if (user) {
      this.loadPayments(user.id);
    }
  }

  loadPayments(parentId: string) {
    this.isLoading = true;
    // Sync in background — don't block loading if it fails
    this.paymentService.syncPayments(parentId).subscribe({ error: () => {} });

    this.paymentService.getPaymentStatus(parentId).subscribe({
      next: (response: any) => {
        this.payments = response.pendingPayments || [];
        this.paidPayments = response.paidPayments || [];
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
    this.paymentService.getPaymentHistory(parentId).subscribe({
      next: (history) => this.paymentHistory = history,
      error: () => {}
    });
  }

  openPaymentModal(payment: any) {
    this.selectedPayment = payment;
    this.showPaymentModal = true;
    this.paymentError = '';
  }

  closePaymentModal() {
    this.showPaymentModal = false;
    this.selectedPayment = null;
    this.cardNumber = '';
    this.expiryDate = '';
    this.cvv = '';
    this.cardholderName = '';
  }

  processPayment() {
    if (!this.paymentService.validateCardNumber(this.cardNumber)) {
      this.paymentError = 'Numéro de carte invalide';
      return;
    }
    if (!this.paymentService.validateExpiryDate(this.expiryDate)) {
      this.paymentError = 'Date d\'expiration invalide (MM/YY)';
      return;
    }
    if (!this.paymentService.validateCVV(this.cvv)) {
      this.paymentError = 'CVV invalide';
      return;
    }

    this.isProcessing = true;
    this.paymentService.processPayment({
      enrollmentId: this.selectedPayment.enrollmentId || this.selectedPayment.id,
      cardNumber: this.cardNumber,
      expiryDate: this.expiryDate,
      cvv: this.cvv,
      cardholderName: this.cardholderName
    }).subscribe({
      next: () => {
        this.isProcessing = false;
        this.closePaymentModal();
        const user = this.authService.currentUser;
        if (user) this.loadPayments(user.id);
      },
      error: (err) => {
        this.isProcessing = false;
        this.paymentError = err.error?.message || 'Erreur lors du paiement';
      }
    });
  }

  formatAmount(amount: number): string {
    return this.paymentService.formatAmount(amount);
  }

  getStatusBadge(status: string): string {
    switch (status) {
      case 'paid': return 'badge-success';
      case 'pending': return 'badge-warning';
      case 'overdue': return 'badge-danger';
      default: return 'badge-info';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'paid': return 'Payé';
      case 'pending': return 'En attente';
      case 'overdue': return 'En retard';
      default: return status;
    }
  }
}
