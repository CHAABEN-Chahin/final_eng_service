import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Review } from '../models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

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

  editReview(reviewId: string, data: { parentId: string; rating: number; comment: string }): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/reviews/${reviewId}`, data);
  }

  deleteReview(reviewId: string, parentId: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/reviews/${reviewId}`, {
      body: { parentId }
    });
  }
}
