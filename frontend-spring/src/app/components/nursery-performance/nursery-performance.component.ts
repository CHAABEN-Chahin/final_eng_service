import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { NurseryService } from '../../services/nursery.service';
import { ReviewService } from '../../services/review.service';

@Component({
  selector: 'app-nursery-performance',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './nursery-performance.component.html',
  styleUrl: './nursery-performance.component.scss'
})
export class NurseryPerformanceComponent implements OnInit {
  reviews: any[] = [];
  averageRating = 0;
  ratingDistribution: { [key: number]: number } = { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 };
  isLoading = true;

  constructor(
    private authService: AuthService,
    private nurseryService: NurseryService,
    private reviewService: ReviewService
  ) {}

  ngOnInit() {
    const user = this.authService.currentUser;
    if (user) {
      this.nurseryService.getNurseriesByOwner(user.id).subscribe({
        next: (nurseries) => {
          if (nurseries.length > 0) {
            this.loadReviews(nurseries[0].id);
          }
          this.isLoading = false;
        },
        error: () => this.isLoading = false
      });
    }
  }

  loadReviews(nurseryId: string) {
    this.reviewService.getNurseryReviews(nurseryId).subscribe({
      next: (reviews) => {
        this.reviews = reviews;
        this.calculateStats();
      },
      error: () => {}
    });
  }

  calculateStats() {
    if (this.reviews.length === 0) return;
    const total = this.reviews.reduce((sum, r) => sum + r.rating, 0);
    this.averageRating = total / this.reviews.length;
    this.ratingDistribution = { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 };
    this.reviews.forEach(r => {
      const rounded = Math.round(r.rating);
      if (this.ratingDistribution[rounded] !== undefined) {
        this.ratingDistribution[rounded]++;
      }
    });
  }

  getStars(rating: number): string {
    return '★'.repeat(Math.round(rating)) + '☆'.repeat(5 - Math.round(rating));
  }

  getPercentage(count: number): number {
    return this.reviews.length > 0 ? (count / this.reviews.length) * 100 : 0;
  }
}
