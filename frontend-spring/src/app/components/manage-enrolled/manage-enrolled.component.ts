import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { NurseryService } from '../../services/nursery.service';
import { EnrollmentService } from '../../services/enrollment.service';

@Component({
  selector: 'app-manage-enrolled',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './manage-enrolled.component.html',
  styleUrl: './manage-enrolled.component.scss'
})
export class ManageEnrolledComponent implements OnInit {
  enrollments: any[] = [];
  isLoading = true;
  activeTab = 'pending';
  nurseryId = '';

  constructor(
    private authService: AuthService,
    private nurseryService: NurseryService,
    private enrollmentService: EnrollmentService
  ) {}

  ngOnInit() {
    const user = this.authService.currentUser;
    if (user) {
      this.nurseryService.getNurseriesByOwner(user.id).subscribe({
        next: (nurseries) => {
          if (nurseries.length > 0) {
            this.nurseryId = nurseries[0].id;
            this.loadEnrollments();
          }
          this.isLoading = false;
        },
        error: () => this.isLoading = false
      });
    }
  }

  loadEnrollments() {
    this.enrollmentService.getEnrollmentsByNursery(this.nurseryId).subscribe({
      next: (enrollments) => this.enrollments = enrollments,
      error: () => {}
    });
  }

  get filteredEnrollments() {
    if (this.activeTab === 'all') return this.enrollments;
    // Map 'accepted' tab to 'active' status from backend
    const statusToFilter = this.activeTab === 'accepted' ? 'active' : this.activeTab;
    return this.enrollments.filter(e => e.status === statusToFilter);
  }

  acceptEnrollment(id: string) {
    this.enrollmentService.acceptEnrollment(id).subscribe({
      next: () => this.loadEnrollments(),
      error: () => {}
    });
  }

  rejectEnrollment(id: string) {
    this.enrollmentService.rejectEnrollment(id).subscribe({
      next: () => this.loadEnrollments(),
      error: () => {}
    });
  }

  getStatusBadge(status: string): string {
    switch (status) {
      case 'active': return 'badge-success';
      case 'accepted': return 'badge-success';
      case 'pending': return 'badge-warning';
      case 'rejected': return 'badge-danger';
      default: return 'badge-info';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'active': return 'Acceptée';
      case 'accepted': return 'Acceptée';
      case 'pending': return 'En attente';
      case 'rejected': return 'Refusée';
      default: return status;
    }
  }
}
