import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { NurseryService } from '../../services/nursery.service';
import { Nursery } from '../../models';

@Component({
  selector: 'app-nursery-search',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './nursery-search.component.html',
  styleUrl: './nursery-search.component.scss'
})
export class NurserySearchComponent implements OnInit {
  nurseries: Nursery[] = [];
  filteredNurseries: Nursery[] = [];
  isLoading = true;
  searchCity = '';
  maxPrice = 0;
  minRating = 0;
  showFilters = false;

  constructor(private nurseryService: NurseryService, private router: Router) {}

  ngOnInit() {
    this.loadNurseries();
  }

  loadNurseries() {
    this.isLoading = true;
    this.nurseryService.searchNurseries().subscribe({
      next: (nurseries) => {
        this.nurseries = nurseries;
        this.filteredNurseries = nurseries;
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  applyFilters() {
    this.isLoading = true;
    const filters: any = {};
    if (this.searchCity) filters.city = this.searchCity;
    if (this.maxPrice > 0) filters.maxPrice = this.maxPrice;
    if (this.minRating > 0) filters.minRating = this.minRating;

    this.nurseryService.searchNurseries(filters).subscribe({
      next: (nurseries) => {
        this.nurseries = nurseries;
        this.filteredNurseries = nurseries;
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  resetFilters() {
    this.searchCity = '';
    this.maxPrice = 0;
    this.minRating = 0;
    this.loadNurseries();
  }

  viewDetails(nurseryId: string) {
    this.router.navigate(['/nursery/details', nurseryId]);
  }

  getStars(rating: number): string {
    return '★'.repeat(Math.round(rating)) + '☆'.repeat(5 - Math.round(rating));
  }
}
