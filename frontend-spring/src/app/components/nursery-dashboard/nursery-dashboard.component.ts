import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NurseryService } from '../../services/nursery.service';
import { EnrollmentService } from '../../services/enrollment.service';
import { PaymentService } from '../../services/payment.service';

@Component({
  selector: 'app-nursery-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './nursery-dashboard.component.html',
  styleUrl: './nursery-dashboard.component.scss'
})
export class NurseryDashboardComponent implements OnInit {
  userName = '';
  nursery: any = null;
  stats: any = {};
  pendingEnrollments: any[] = [];
  schedule: any[] = [];
  isLoading = true;

  constructor(
    private authService: AuthService,
    private nurseryService: NurseryService,
    private enrollmentService: EnrollmentService,
    private paymentService: PaymentService,
    private router: Router
  ) {}

  ngOnInit() {
    const user = this.authService.currentUser;
    if (!user) return;
    this.userName = user.name;
    this.loadNurseryData(user.id);
  }

  loadNurseryData(ownerId: string) {
    this.isLoading = true;
    this.nurseryService.getNurseriesByOwner(ownerId).subscribe({
      next: (nurseries) => {
        if (nurseries.length > 0) {
          this.nursery = nurseries[0];
          this.loadDashboardData(this.nursery.id);
        } else {
          this.router.navigate(['/nursery/setup']);
        }
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  loadDashboardData(nurseryId: string) {
    this.nurseryService.getDashboardStats(nurseryId).subscribe({
      next: (response) => this.stats = response.stats || response.statistics || response || {},
      error: () => {}
    });

    this.enrollmentService.getEnrollmentsByNursery(nurseryId).subscribe({
      next: (enrollments) => {
        this.pendingEnrollments = enrollments.filter((e: any) => e.status === 'pending');
      },
      error: () => {}
    });

    this.nurseryService.getNurserySchedule(nurseryId).subscribe({
      next: (response) => this.schedule = response.schedule || response || [],
      error: () => {}
    });
  }

  acceptEnrollment(enrollmentId: string) {
    this.enrollmentService.acceptEnrollment(enrollmentId).subscribe({
      next: () => {
        this.pendingEnrollments = this.pendingEnrollments.filter(e => e.id !== enrollmentId);
      },
      error: () => {}
    });
  }

  rejectEnrollment(enrollmentId: string) {
    this.enrollmentService.rejectEnrollment(enrollmentId).subscribe({
      next: () => {
        this.pendingEnrollments = this.pendingEnrollments.filter(e => e.id !== enrollmentId);
      },
      error: () => {}
    });
  }

  navigateTo(path: string) { this.router.navigate([path]); }
}
