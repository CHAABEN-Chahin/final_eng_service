import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { NurseryService } from '../../services/nursery.service';

@Component({
  selector: 'app-nursery-statistics',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './nursery-statistics.component.html',
  styleUrl: './nursery-statistics.component.scss'
})
export class NurseryStatisticsComponent implements OnInit {
  stats: any = {};
  isLoading = true;

  constructor(private authService: AuthService, private nurseryService: NurseryService) {}

  ngOnInit() {
    const user = this.authService.currentUser;
    if (user) {
      this.nurseryService.getNurseriesByOwner(user.id).subscribe({
        next: (nurseries) => {
          if (nurseries.length > 0) {
            this.nurseryService.getNurseryStats(nurseries[0].id).subscribe({
              next: (response) => {
                const s = response.statistics || response || {};
                this.stats = {
                  totalEnrolled: s.totalEnrollments || s.activeEnrollments || s.total_enrolled || 0,
                  pendingEnrollments: s.pendingEnrollments || s.pending_enrollments || 0,
                  averageRating: s.averageRating || s.average_rating || 0,
                  totalReviews: s.totalReviews || s.total_reviews || 0,
                  totalRevenue: s.totalRevenue || s.total_revenue || 0,
                  availableSpots: s.totalCapacity ? (s.totalCapacity - (s.capacityUsed || 0)) : (s.availableSpots || s.available_spots || 0),
                  ageDistribution: this.buildAgeDistribution(s.childrenByAgeGroup || s.age_distribution || s.ageDistribution)
                };
                this.isLoading = false;
              },
              error: () => this.isLoading = false
            });
          } else {
            this.isLoading = false;
          }
        },
        error: () => this.isLoading = false
      });
    }
  }

  private buildAgeDistribution(data: any): any[] {
    if (Array.isArray(data)) return data;
    if (data && typeof data === 'object') {
      return Object.entries(data).map(([group, count]) => ({ ageGroup: group, count }));
    }
    return [];
  }
}
