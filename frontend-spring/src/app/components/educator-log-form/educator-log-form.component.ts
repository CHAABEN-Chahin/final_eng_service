import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { DailyLogService } from '../../services/daily-log.service';
import { ChildService } from '../../services/child.service';
import { NurseryService } from '../../services/nursery.service';

interface PulseField {
  key: string;
  label: string;
  icon: string;
}

@Component({
  selector: 'app-educator-log-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './educator-log-form.component.html',
  styleUrl: './educator-log-form.component.scss'
})
export class EducatorLogFormComponent implements OnInit {
  childId = '';
  childName = '';
  nurseryId = '';
  isLoading = true;
  isSaving = false;
  saveSuccess = false;
  saveError = '';

  // All pulse dimensions
  pulseFields: PulseField[] = [
    { key: 'mood', label: 'Humeur', icon: '🫀' },
    { key: 'appetite', label: 'Appétit', icon: '🍽️' },
    { key: 'energy', label: 'Énergie', icon: '⚡' },
    { key: 'sociability', label: 'Sociabilité', icon: '🤝' },
    { key: 'concentration', label: 'Concentration', icon: '🎯' },
    { key: 'autonomy', label: 'Autonomie', icon: '🧩' },
    { key: 'sleep', label: 'Sommeil / Sieste', icon: '😴' },
    { key: 'motorSkills', label: 'Motricité', icon: '🏃' }
  ];

  // Pulse scores (1-5), default 3
  scores: Record<string, number> = {
    mood: 3, appetite: 3, energy: 3,
    sociability: 3, concentration: 3, autonomy: 3,
    sleep: 3, motorSkills: 3
  };

  // Word labels per dimension (1 to 5)
  scoreLabels: Record<string, string[]> = {
    mood:          ['Triste', 'Maussade', 'Neutre', 'Content', 'Joyeux'],
    appetite:      ['Très faible', 'Faible', 'Moyen', 'Bon', 'Excellent'],
    energy:        ['Très faible', 'Faible', 'Modérée', 'Bonne', 'Débordante'],
    sociability:   ['Isolé', 'Réservé', 'Normal', 'Sociable', 'Très sociable'],
    concentration: ['Très dispersé', 'Dispersé', 'Variable', 'Concentré', 'Très concentré'],
    autonomy:      ['Très dépendant', 'Dépendant', 'Variable', 'Autonome', 'Très autonome'],
    sleep:         ['Très agité', 'Agité', 'Moyen', 'Bon', 'Excellent'],
    motorSkills:   ['Très faible', 'Faible', 'Moyenne', 'Bonne', 'Excellente']
  };

  // Tag cloud
  availableTags = [
    { emoji: '🎨', label: 'Créatif', key: 'creative' },
    { emoji: '🤝', label: 'Serviable', key: 'helpful' },
    { emoji: '📖', label: 'Attentif', key: 'attentive' },
    { emoji: '🏃', label: 'Actif', key: 'active' },
    { emoji: '🧩', label: 'Résolveur', key: 'problem-solver' },
    { emoji: '😊', label: 'Sociable', key: 'sociable' },
    { emoji: '😴', label: 'Fatigué', key: 'tired' },
    { emoji: '🌟', label: 'Confiant', key: 'confident' },
    { emoji: '🎵', label: 'Musical', key: 'musical' },
    { emoji: '🧸', label: 'Câlin', key: 'cuddly' },
    { emoji: '💬', label: 'Bavard', key: 'talkative' },
    { emoji: '🤫', label: 'Calme', key: 'calm' },
    { emoji: '😤', label: 'Agité', key: 'agitated' },
    { emoji: '🧠', label: 'Curieux', key: 'curious' }
  ];
  selectedTags: string[] = [];

  // Remarks
  remarks = '';

  // Internal toggle
  isInternal = false;

  // Existing logs for this child today
  existingLogs: any[] = [];
  date = new Date().toISOString().split('T')[0];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private dailyLogService: DailyLogService,
    private childService: ChildService,
    private nurseryService: NurseryService
  ) {}

  ngOnInit() {
    this.childId = this.route.snapshot.paramMap.get('childId') || '';
    const user = this.authService.currentUser;

    if (!user || !this.childId) {
      this.router.navigate(['/nursery/children-list']);
      return;
    }

    // Load nursery info
    this.nurseryService.getNurseriesByOwner(user.id).subscribe({
      next: (nurseries) => {
        if (nurseries.length > 0) {
          this.nurseryId = nurseries[0].id;
        }
        this.loadChildInfo();
      },
      error: () => this.loadChildInfo()
    });
  }

  loadChildInfo() {
    // Load existing logs
    this.dailyLogService.getLogsByChild(this.childId).subscribe({
      next: (logs) => {
        const today = new Date().toISOString().split('T')[0];
        this.existingLogs = logs.filter(l => l.date === today);
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });

    // Try to get child name from route state or sessionStorage
    this.childName = history.state?.childName || sessionStorage.getItem('currentChildName') || 'Enfant';
  }

  toggleTag(key: string) {
    const index = this.selectedTags.indexOf(key);
    if (index >= 0) {
      this.selectedTags.splice(index, 1);
    } else {
      this.selectedTags.push(key);
    }
  }

  isTagSelected(key: string): boolean {
    return this.selectedTags.includes(key);
  }

  setScore(field: string, value: number) {
    this.scores[field] = value;
  }

  getLabel(field: string, index: number): string {
    return this.scoreLabels[field]?.[index] || '';
  }

  submitLog() {
    if (this.isSaving) return;

    const user = this.authService.currentUser;
    if (!user) return;

    this.isSaving = true;
    this.saveSuccess = false;
    this.saveError = '';

    const logData = {
      childId: this.childId,
      nurseryId: this.nurseryId,
      educatorId: user.id,
      date: new Date().toISOString().split('T')[0],
      mood: this.scores['mood'],
      appetite: this.scores['appetite'],
      energy: this.scores['energy'],
      sociability: this.scores['sociability'],
      concentration: this.scores['concentration'],
      autonomy: this.scores['autonomy'],
      sleep: this.scores['sleep'],
      motorSkills: this.scores['motorSkills'],
      tags: this.selectedTags,
      remarks: this.remarks || undefined,
      isInternal: this.isInternal
    };

    this.dailyLogService.createLog(logData).subscribe({
      next: () => {
        this.isSaving = false;
        this.saveSuccess = true;
        // Reset form
        for (const key of Object.keys(this.scores)) {
          this.scores[key] = 3;
        }
        this.selectedTags = [];
        this.remarks = '';
        this.isInternal = false;
        // Reload existing logs
        this.loadChildInfo();
      },
      error: () => {
        this.isSaving = false;
        this.saveError = 'Erreur lors de la sauvegarde. Veuillez réessayer.';
      }
    });
  }

  goBack() {
    this.router.navigate(['/nursery/espace-enfant', this.childId]);
  }
}
