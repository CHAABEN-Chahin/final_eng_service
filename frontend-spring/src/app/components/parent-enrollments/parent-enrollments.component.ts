import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { EnrollmentService } from '../../services/enrollment.service';

@Component({
  selector: 'app-parent-enrollments',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './parent-enrollments.component.html',
  styleUrl: './parent-enrollments.component.scss'
})
export class ParentEnrollmentsComponent implements OnInit {
  enrollments: any[] = [];
  isLoading = true;

  constructor(private authService: AuthService, private enrollmentService: EnrollmentService) {}

  ngOnInit() {
    const user = this.authService.currentUser;
    if (user) {
      this.enrollmentService.getEnrollmentsByParent(user.id).subscribe({
        next: (enrollments) => { this.enrollments = enrollments; this.isLoading = false; },
        error: () => this.isLoading = false
      });
    }
  }

  getStatusBadge(status: string): string {
    switch (status) {
      case 'accepted': return 'badge-success';
      case 'pending': return 'badge-warning';
      case 'rejected': return 'badge-danger';
      default: return 'badge-info';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'accepted': return 'Acceptée';
      case 'pending': return 'En attente';
      case 'rejected': return 'Refusée';
      default: return status;
    }
  }
}
