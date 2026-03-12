import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Nursery, Review } from '../models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class NurseryService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  createNursery(data: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/nurseries`, data);
  }

  searchNurseries(filters?: { city?: string; maxPrice?: number; minRating?: number; minSpots?: number }): Observable<Nursery[]> {
    let params = new HttpParams();
    if (filters?.city) params = params.set('city', filters.city);
    if (filters?.maxPrice) params = params.set('max_price', filters.maxPrice.toString());
    if (filters?.minRating) params = params.set('min_rating', filters.minRating.toString());
    if (filters?.minSpots) params = params.set('min_spots', filters.minSpots.toString());

    return this.http.get<any>(`${this.apiUrl}/nurseries`, { params }).pipe(
      map(response => {
        const nurseries = response.nurseries || response || [];
        return nurseries.map((n: any) => this.mapNursery(n));
      })
    );
  }

  getNurseryById(id: string): Observable<Nursery> {
    return this.http.get<any>(`${this.apiUrl}/nurseries/${id}`).pipe(
      map(response => this.mapNursery(response.nursery || response))
    );
  }

  getNurseriesByOwner(ownerId: string): Observable<Nursery[]> {
    return this.http.get<any>(`${this.apiUrl}/nurseries/owner/${ownerId}`).pipe(
      map(response => {
        const nurseries = response.nurseries || response || [];
        return nurseries.map((n: any) => this.mapNursery(n));
      })
    );
  }

  getNurseryReviews(nurseryId: string): Observable<Review[]> {
    return this.http.get<any>(`${this.apiUrl}/nurseries/${nurseryId}/reviews`).pipe(
      map(response => {
        const reviews = response.reviews || response || [];
        return reviews.map((r: any) => ({
          id: r.id?.toString() || '',
          parentName: r.parent_name || r.parentName || 'Parent',
          rating: parseFloat(r.rating) || 0,
          comment: r.comment || '',
          date: r.created_at || r.createdAt || r.date || ''
        }));
      })
    );
  }

  postReview(nurseryId: string, data: { parentId: string; parentName: string; rating: number; comment: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/nurseries/${nurseryId}/reviews`, data);
  }

  getNurseryStats(nurseryId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/nurseries/${nurseryId}/statistics`);
  }

  getNurserySchedule(nurseryId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/nurseries/${nurseryId}/schedule`);
  }

  createScheduleItem(nurseryId: string, data: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/nurseries/${nurseryId}/schedule`, data);
  }

  getEnrolledChildren(nurseryId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/nurseries/${nurseryId}/enrolled-children`);
  }

  getDashboardStats(nurseryId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/nurseries/${nurseryId}/statistics`);
  }

  private mapNursery(n: any): Nursery {
    return {
      id: n.id?.toString() || '',
      name: n.name || '',
      address: n.address || '',
      city: n.city || '',
      postalCode: n.postalCode || n.postal_code || '',
      distance: parseFloat(n.distance) || 0,
      rating: parseFloat(n.rating) || 0,
      reviewCount: parseInt(n.reviewCount || n.review_count) || 0,
      price: parseFloat(n.pricePerMonth || n.price_per_month || n.price) || 0,
      availableSpots: parseInt(n.availableSpots || n.available_spots) || 0,
      totalSpots: parseInt(n.totalSpots || n.total_spots) || 0,
      hours: n.hours || '08:00 - 18:00',
      photo: n.photoUrl || n.photo || '',
      description: n.description || '',
      activities: n.activities || [],
      facilities: n.facilities || [],
      staff: parseInt(n.staffCount || n.staff_count || n.staff) || 0,
      ageRange: n.ageRange || n.age_range || '0-5 ans',
      phone: n.phone || '',
      email: n.email || '',
      ownerId: n.ownerId || n.owner_id || '',
      reviews: n.reviews || []
    };
  }
}
