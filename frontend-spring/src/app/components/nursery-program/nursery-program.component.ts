import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { NurseryService } from '../../services/nursery.service';
import { ScheduleService } from '../../services/schedule.service';

@Component({
  selector: 'app-nursery-program',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './nursery-program.component.html',
  styleUrl: './nursery-program.component.scss'
})
export class NurseryProgramComponent implements OnInit {
  schedule: any[] = [];
  isLoading = true;
  nurseryId = '';
  showAddForm = false;
  newTimeSlot = '';
  newActivity = '';
  newDescription = '';

  constructor(
    private authService: AuthService,
    private nurseryService: NurseryService,
    private scheduleService: ScheduleService
  ) {}

  ngOnInit() {
    const user = this.authService.currentUser;
    if (user) {
      this.nurseryService.getNurseriesByOwner(user.id).subscribe({
        next: (nurseries) => {
          if (nurseries.length > 0) {
            this.nurseryId = nurseries[0].id;
            this.loadSchedule();
          }
          this.isLoading = false;
        },
        error: () => this.isLoading = false
      });
    }
  }

  loadSchedule() {
    this.nurseryService.getNurserySchedule(this.nurseryId).subscribe({
      next: (response) => this.schedule = response.schedule || response || [],
      error: () => {}
    });
  }

  addScheduleItem() {
    if (!this.newTimeSlot || !this.newActivity) return;
    this.nurseryService.createScheduleItem(this.nurseryId, {
      timeSlot: this.newTimeSlot,
      activityName: this.newActivity,
      description: this.newDescription
    }).subscribe({
      next: () => {
        this.loadSchedule();
        this.showAddForm = false;
        this.newTimeSlot = '';
        this.newActivity = '';
        this.newDescription = '';
      },
      error: () => {}
    });
  }

  deleteScheduleItem(id: string) {
    this.scheduleService.deleteScheduleItem(id).subscribe({
      next: () => this.loadSchedule(),
      error: () => {}
    });
  }
}
