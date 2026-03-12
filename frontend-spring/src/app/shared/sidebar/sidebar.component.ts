import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent implements OnInit {
  unreadNotifications = 0;
  mobileMenuOpen = false;

  constructor(
    public authService: AuthService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadUnreadCount();
  }

  loadUnreadCount() {
    const user = this.authService.currentUser;
    if (user) {
      this.notificationService.getUnreadCount(user.id).subscribe({
        next: (count) => this.unreadNotifications = count,
        error: () => {}
      });
    }
  }

  get isParent(): boolean {
    return this.authService.currentUser?.type === 'parent';
  }

  get isNursery(): boolean {
    return this.authService.currentUser?.type === 'nursery';
  }

  get userName(): string {
    return this.authService.currentUser?.name || '';
  }

  get userEmail(): string {
    return this.authService.currentUser?.email || '';
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/auth']);
  }

  toggleMobileMenu() {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }
}
