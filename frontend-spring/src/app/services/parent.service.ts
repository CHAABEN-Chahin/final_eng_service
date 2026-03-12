import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ParentService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getParentNurseries(parentId: string): Observable<any[]> {
    return this.http.get<any>(`${this.apiUrl}/parents/${parentId}/nurseries`).pipe(
      map(response => response.nurseries || response || [])
    );
  }

  getTodayProgram(parentId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/parents/${parentId}/today-program`).pipe(
      map(response => response.program || response.programs || response || [])
    );
  }

  getNurseryRecentReviews(parentId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/parents/${parentId}/nursery-reviews`).pipe(
      map(response => response.reviews || response || [])
    );
  }
}
