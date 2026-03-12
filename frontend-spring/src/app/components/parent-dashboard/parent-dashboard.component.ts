import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ParentService } from '../../services/parent.service';
import { ChildService } from '../../services/child.service';
import { EnrollmentService } from '../../services/enrollment.service';
import { NotificationService } from '../../services/notification.service';
import { ConversationService } from '../../services/conversation.service';

@Component({
  selector: 'app-parent-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './parent-dashboard.component.html',
  styleUrl: './parent-dashboard.component.scss'
})
export class ParentDashboardComponent implements OnInit {
  userName = '';
  children: any[] = [];
  enrolledNurseries: any[] = [];
  todayProgram: any[] = [];
  recentReviews: any[] = [];
  unreadNotifications = 0;
  isLoading = true;

  constructor(
    private authService: AuthService,
    private parentService: ParentService,
    private childService: ChildService,
    private enrollmentService: EnrollmentService,
    private notificationService: NotificationService,
    private conversationService: ConversationService,
    private router: Router
  ) {}

  ngOnInit() {
    const user = this.authService.currentUser;
    if (!user) return;
    this.userName = user.name;
    this.loadDashboardData(user.id);
  }

  loadDashboardData(userId: string) {
    this.isLoading = true;

    this.childService.getChildrenByParent(userId).subscribe({
      next: (children) => this.children = children,
      error: () => {}
    });

    this.parentService.getParentNurseries(userId).subscribe({
      next: (nurseries) => this.enrolledNurseries = nurseries,
      error: () => {}
    });

    this.parentService.getTodayProgram(userId).subscribe({
      next: (programs) => this.todayProgram = Array.isArray(programs) ? programs : [],
      error: () => {}
    });

    this.parentService.getNurseryRecentReviews(userId).subscribe({
      next: (reviews) => this.recentReviews = Array.isArray(reviews) ? reviews : [],
      error: () => {}
    });

    this.notificationService.getUnreadCount(userId).subscribe({
      next: (count) => this.unreadNotifications = count,
      error: () => {}
    });

    setTimeout(() => this.isLoading = false, 1000);
  }

  getStars(rating: number): string {
    return '★'.repeat(Math.round(rating)) + '☆'.repeat(5 - Math.round(rating));
  }

  navigateToSearch() { this.router.navigate(['/search']); }
  navigateToChildren() { this.router.navigate(['/parent/children']); }
  navigateToEnrollments() { this.router.navigate(['/parent/enrollments']); }
  navigateToPayments() { this.router.navigate(['/parent/payments']); }
  navigateToReviews() { this.router.navigate(['/parent/reviews']); }
  navigateToNotifications() { this.router.navigate(['/notifications']); }
  navigateToChat() { this.router.navigate(['/chat']); }
  
  contactNursery(nurseryId: string) {
    const user = this.authService.currentUser;
    if (!user) return;
    
    // Find the nursery to get owner info
    const nursery = this.enrolledNurseries.find(n => (n.id || n.nursery_id) === nurseryId);
    if (!nursery) return;
    
    // Store nursery owner ID and nursery ID in session storage for the chat component to use
    if (nursery.ownerId) {
      sessionStorage.setItem('chatRecipientId', nursery.ownerId);
    }
    sessionStorage.setItem('chatParentId', user.id);
    sessionStorage.setItem('chatNurseryId', nurseryId);
    
    this.conversationService.getOrCreateConversation(user.id, '', nurseryId).subscribe({
      next: (conversation) => {
        this.router.navigate(['/chat', conversation.id]);
      },
      error: (err) => {
        console.error('Error creating conversation:', err);
      }
    });
  }
  
  viewNurseryDetails(nurseryId: string) { this.router.navigate(['/nursery/details', nurseryId]); }
}
