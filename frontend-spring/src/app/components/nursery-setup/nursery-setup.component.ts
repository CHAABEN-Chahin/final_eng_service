import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NurseryService } from '../../services/nursery.service';

@Component({
  selector: 'app-nursery-setup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './nursery-setup.component.html',
  styleUrl: './nursery-setup.component.scss'
})
export class NurserySetupComponent {
  nurseryName = '';
  address = '';
  city = '';
  phone = '';
  email = '';
  description = '';
  pricePerMonth = '';
  totalSpots = '';
  ageRange = '0-5 ans';
  hours = '08:00 - 18:00';
  facilities = '';
  activities = '';
  isLoading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private nurseryService: NurseryService,
    private router: Router
  ) {}

  onSubmit() {
    if (!this.nurseryName || !this.address || !this.city) {
      this.errorMessage = 'Veuillez remplir tous les champs obligatoires';
      return;
    }

    const user = this.authService.currentUser;
    if (!user) return;

    this.isLoading = true;
    this.errorMessage = '';

    this.nurseryService.createNursery({
      name: this.nurseryName,
      address: this.address,
      city: this.city,
      phone: this.phone,
      email: this.email,
      description: this.description,
      price_per_month: parseFloat(this.pricePerMonth) || 0,
      total_spots: parseInt(this.totalSpots) || 20,
      age_range: this.ageRange,
      hours: this.hours,
      facilities: this.facilities.split(',').map(f => f.trim()).filter(f => f),
      activities: this.activities.split(',').map(a => a.trim()).filter(a => a),
      owner_id: user.id
    }).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/nursery/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Erreur lors de la création';
      }
    });
  }
}
