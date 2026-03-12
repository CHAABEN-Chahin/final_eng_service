import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { DailyLogService } from '../../services/daily-log.service';
import { DailyLog } from '../../models';

@Component({
  selector: 'app-parent-daily-story',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './parent-daily-story.component.html',
  styleUrl: './parent-daily-story.component.scss'
})
export class ParentDailyStoryComponent implements OnInit {
  childId = '';
  childName = '';
  isLoading = true;
  todayLog: DailyLog | null = null;
  previousLogs: DailyLog[] = [];

  tagLabels: Record<string, string> = {
    'creative': '🎨 Créatif',
    'helpful': '🤝 Serviable',
    'attentive': '📖 Attentif',
    'active': '🏃 Actif',
    'problem-solver': '🧩 Résolveur',
    'sociable': '😊 Sociable',
    'tired': '😴 Fatigué',
    'confident': '🌟 Confiant',
    'musical': '🎵 Musical',
    'cuddly': '🧸 Câlin',
    'talkative': '💬 Bavard',
    'calm': '🤫 Calme',
    'agitated': '😤 Agité',
    'curious': '🧠 Curieux'
  };

  allScoreDimensions = [
    { key: 'mood', label: 'Humeur', icon: '' },
    { key: 'appetite', label: 'Appétit', icon: '🍽️' },
    { key: 'energy', label: 'Énergie', icon: '⚡' },
    { key: 'sociability', label: 'Sociabilité', icon: '🤝' },
    { key: 'concentration', label: 'Concentration', icon: '🎯' },
    { key: 'autonomy', label: 'Autonomie', icon: '🧩' },
    { key: 'sleep', label: 'Sommeil', icon: '😴' },
    { key: 'motorSkills', label: 'Motricité', icon: '🏃' }
  ];

  getScoreValue(log: DailyLog, key: string): number {
    return (log as any)[key] || 3;
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private dailyLogService: DailyLogService
  ) {}

  ngOnInit() {
    this.childId = this.route.snapshot.paramMap.get('childId') || '';
    this.childName = history.state?.childName || sessionStorage.getItem('currentChildName') || 'Mon enfant';

    if (!this.childId) {
      this.router.navigate(['/parent/children']);
      return;
    }

    this.loadLogs();
  }

  loadLogs() {
    // Load today's log
    this.dailyLogService.getTodayLog(this.childId).subscribe({
      next: (log) => {
        this.todayLog = log;
        this.loadPreviousLogs();
      },
      error: () => this.loadPreviousLogs()
    });
  }

  loadPreviousLogs() {
    this.dailyLogService.getLogsByChildForParent(this.childId).subscribe({
      next: (logs) => {
        this.previousLogs = logs;
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  getTagLabel(tag: string): string {
    return this.tagLabels[tag] || tag;
  }

  getScorePercent(score: number): number {
    return (score / 5) * 100;
  }

  getScoreColor(score: number): string {
    if (score >= 4) return '#059669';
    if (score >= 3) return '#0891b2';
    if (score >= 2) return '#f59e0b';
    return '#ef4444';
  }

  getMoodEmoji(score: number): string {
    const emojis = ['😢', '😕', '😐', '🙂', '😄'];
    return emojis[Math.min(score - 1, 4)] || '😐';
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' });
  }

  goBack() {
    this.router.navigate(['/parent/espace-enfant', this.childId]);
  }
}
