import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { NurseryService } from '../../services/nursery.service';

@Component({
  selector: 'app-nursery-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './nursery-settings.component.html',
  styleUrl: './nursery-settings.component.scss'
})
export class NurserySettingsComponent implements OnInit {
  nursery: any = null;
  userName = '';
  userPhone = '';
  isLoading = true;
  isSaving = false;
  successMessage = '';

  constructor(private authService: AuthService, private nurseryService: NurseryService) {}

  ngOnInit() {
    const user = this.authService.currentUser;
    if (user) {
      this.userName = user.name;
      this.userPhone = user.phone || '';
      this.nurseryService.getNurseriesByOwner(user.id).subscribe({
        next: (nurseries) => {
          if (nurseries.length > 0) this.nursery = nurseries[0];
          this.isLoading = false;
        },
        error: () => this.isLoading = false
      });
    }
  }

  saveProfile() {
    const user = this.authService.currentUser;
    if (!user) return;
    this.isSaving = true;
    this.authService.updateUser(user.id, { name: this.userName, phone: this.userPhone }).subscribe({
      next: () => {
        this.isSaving = false;
        this.successMessage = 'Profil mis à jour avec succès';
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: () => this.isSaving = false
    });
  }
}
