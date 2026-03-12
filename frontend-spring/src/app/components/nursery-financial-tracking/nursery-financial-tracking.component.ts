import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { PaymentService } from '../../services/payment.service';

@Component({
  selector: 'app-nursery-financial-tracking',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './nursery-financial-tracking.component.html',
  styleUrl: './nursery-financial-tracking.component.scss'
})
export class NurseryFinancialTrackingComponent implements OnInit {
  payments: any[] = [];
  stats: any = {};
  isLoading = true;

  constructor(private authService: AuthService, private paymentService: PaymentService) {}

  ngOnInit() {
    const user = this.authService.currentUser;
    if (user) {
      this.paymentService.getOwnerPayments(user.id).subscribe({
        next: (payments) => {
          this.payments = payments.map((p: any) => ({
            ...p,
            status: p.paymentStatus || p.status || 'unpaid',
            parentName: p.parentName || p.parent_name || 'N/A',
            childName: p.childName || p.child_name || 'N/A'
          }));
          this.isLoading = false;
        },
        error: () => this.isLoading = false
      });
      this.paymentService.getOwnerFinancialStats(user.id).subscribe({
        next: (response: any) => {
          this.stats = {
            totalRevenue: response.totalReceived || response.totalRevenue || response.total_revenue || 0,
            pendingPayments: response.totalPending || response.pendingPayments || response.pending_payments || 0,
            completedPayments: response.paidCount || response.completedPayments || response.completed_payments || 0
          };
        },
        error: () => {}
      });
    }
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
