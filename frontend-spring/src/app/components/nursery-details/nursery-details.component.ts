import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { NurseryService } from '../../services/nursery.service';
import { AuthService } from '../../services/auth.service';
import { ConversationService } from '../../services/conversation.service';
import { Nursery, Review } from '../../models';

@Component({
  selector: 'app-nursery-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './nursery-details.component.html',
  styleUrl: './nursery-details.component.scss'
})
export class NurseryDetailsComponent implements OnInit {
  nursery: Nursery | null = null;
  reviews: Review[] = [];
  isLoading = true;
  isFavorite = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private nurseryService: NurseryService,
    private authService: AuthService,
    private conversationService: ConversationService
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadNursery(id);
    }
  }

  loadNursery(id: string) {
    this.isLoading = true;
    this.nurseryService.getNurseryById(id).subscribe({
      next: (nursery) => {
        this.nursery = nursery;
        this.loadReviews(id);
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  loadReviews(nurseryId: string) {
    this.nurseryService.getNurseryReviews(nurseryId).subscribe({
      next: (reviews) => this.reviews = reviews,
      error: () => {}
    });
  }

  toggleFavorite() {
    this.isFavorite = !this.isFavorite;
  }

  enrollChild() {
    if (this.nursery) {
      this.router.navigate(['/enrollment', this.nursery.id]);
    }
  }

  contactNursery() {
    const user = this.authService.currentUser;
    if (!user || !this.nursery) return;
    
    // Store recipient ID and nursery ID in session storage for the chat component
    if (this.nursery.ownerId) {
      sessionStorage.setItem('chatRecipientId', this.nursery.ownerId);
    }
    sessionStorage.setItem('chatParentId', user.id);
    sessionStorage.setItem('chatNurseryId', this.nursery.id);
    
    this.conversationService.getOrCreateConversation(user.id, '', this.nursery.id).subscribe({
      next: (conversation) => {
        this.router.navigate(['/chat', conversation.id]);
      },
      error: (err) => {
        console.error('Error creating conversation:', err);
      }
    });
  }

  goBack() {
    this.router.navigate(['/search']);
  }

  getStars(rating: number): string {
    return '★'.repeat(Math.round(rating)) + '☆'.repeat(5 - Math.round(rating));
  }
}
