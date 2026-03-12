import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ParentService } from '../../services/parent.service';
import { ReviewService } from '../../services/review.service';

@Component({
  selector: 'app-parent-reviews',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './parent-reviews.component.html',
  styleUrl: './parent-reviews.component.scss'
})
export class ParentReviewsComponent implements OnInit {
  nurseries: any[] = [];
  isLoading = true;
  showReviewModal = false;
  selectedNursery: any = null;
  reviewRating = 5;
  reviewComment = '';
  isSubmitting = false;

  constructor(
    private authService: AuthService,
    private parentService: ParentService,
    private reviewService: ReviewService
  ) {}

  ngOnInit() {
    const user = this.authService.currentUser;
    if (user) {
      this.parentService.getParentNurseries(user.id).subscribe({
        next: (nurseries) => { this.nurseries = nurseries; this.isLoading = false; },
        error: () => this.isLoading = false
      });
    }
  }

  openReviewModal(nursery: any) {
    this.selectedNursery = nursery;
    this.showReviewModal = true;
    this.reviewRating = 5;
    this.reviewComment = '';
  }

  closeReviewModal() {
    this.showReviewModal = false;
    this.selectedNursery = null;
  }

  setRating(rating: number) {
    this.reviewRating = rating;
  }

  submitReview() {
    const user = this.authService.currentUser;
    if (!user || !this.selectedNursery) return;

    this.isSubmitting = true;
    const nurseryId = this.selectedNursery.id || this.selectedNursery.nursery_id;

    this.reviewService.postReview(nurseryId, {
      parentId: user.id,
      parentName: user.name,
      rating: this.reviewRating,
      comment: this.reviewComment
    }).subscribe({
      next: () => { this.isSubmitting = false; this.closeReviewModal(); },
      error: () => this.isSubmitting = false
    });
  }

  getStars(rating: number): number[] {
    return Array.from({ length: 5 }, (_, i) => i + 1);
  }
}
