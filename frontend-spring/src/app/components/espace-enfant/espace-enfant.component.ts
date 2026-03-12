import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { DailyLogService } from '../../services/daily-log.service';
import { DailyLog } from '../../models';

@Component({
  selector: 'app-espace-enfant',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './espace-enfant.component.html',
  styleUrl: './espace-enfant.component.scss'
})
export class EspaceEnfantComponent implements OnInit {
  childId = '';
  childName = '';
  childAge = '';
  userType = '';
  isLoading = true;
  todayLog: DailyLog | null = null;
  recentLogs: DailyLog[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private dailyLogService: DailyLogService
  ) {}

  ngOnInit() {
    this.childId = this.route.snapshot.paramMap.get('childId') || '';
    this.childName = history.state?.childName || sessionStorage.getItem('currentChildName') || 'Enfant';
    this.childAge = history.state?.childAge || sessionStorage.getItem('currentChildAge') || '';

    // Persist for sub-navigation
    sessionStorage.setItem('currentChildName', this.childName);
    if (this.childAge) sessionStorage.setItem('currentChildAge', this.childAge);

    const user = this.authService.currentUser;
    if (!user || !this.childId) {
      this.router.navigate(['/welcome']);
      return;
    }

    this.userType = user.type;
    this.loadData();
  }

  loadData() {
    // Load today's log
    this.dailyLogService.getTodayLog(this.childId).subscribe({
      next: (log) => {
        this.todayLog = log;
        this.loadRecent();
      },
      error: () => this.loadRecent()
    });
  }

  loadRecent() {
    const obs$ = this.userType === 'parent'
      ? this.dailyLogService.getLogsByChildForParent(this.childId)
      : this.dailyLogService.getLogsByChild(this.childId);

    obs$.subscribe({
      next: (logs) => {
        this.recentLogs = logs.slice(0, 5);
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  get isNursery(): boolean {
    return this.userType === 'nursery';
  }

  get isParent(): boolean {
    return this.userType === 'parent';
  }

  // Navigation methods
  openDailyLog() {
    if (this.isNursery) {
      this.router.navigate(['/nursery/espace-enfant', this.childId, 'log'], {
        state: { childName: this.childName }
      });
    }
  }

  openDailyStory() {
    this.router.navigate(['/parent/espace-enfant', this.childId, 'story'], {
      state: { childName: this.childName }
    });
  }

  openChat() {
    const prefix = this.isNursery ? '/nursery' : '/parent';
    this.router.navigate([prefix + '/espace-enfant', this.childId, 'chat'], {
      state: { childName: this.childName }
    });
  }

  goBack() {
    if (this.isNursery) {
      this.router.navigate(['/nursery/children-list']);
    } else {
      this.router.navigate(['/parent/children']);
    }
  }

  getMoodEmoji(score: number): string {
    const emojis = ['😢', '😕', '😐', '🙂', '😄'];
    return emojis[Math.min(score - 1, 4)] || '😐';
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('fr-FR', { weekday: 'short', day: 'numeric', month: 'short' });
  }
}
