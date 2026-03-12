import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { DailyLog } from '../models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class DailyLogService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  createLog(logData: {
    childId: string;
    nurseryId: string;
    educatorId: string;
    date: string;
    mood: number;
    appetite: number;
    energy: number;
    sociability: number;
    concentration: number;
    autonomy: number;
    sleep: number;
    motorSkills: number;
    tags: string[];
    remarks?: string;
    isInternal: boolean;
  }): Observable<DailyLog> {
    return this.http.post<any>(`${this.apiUrl}/daily-logs`, logData).pipe(
      map(response => this.mapLog(response.log || response))
    );
  }

  getLogsByChild(childId: string): Observable<DailyLog[]> {
    return this.http.get<any>(`${this.apiUrl}/daily-logs/child/${childId}`).pipe(
      map(response => {
        const logs = response.logs || response || [];
        return logs.map((l: any) => this.mapLog(l));
      })
    );
  }

  getLogsByChildForParent(childId: string): Observable<DailyLog[]> {
    return this.http.get<any>(`${this.apiUrl}/daily-logs/child/${childId}/parent`).pipe(
      map(response => {
        const logs = response.logs || response || [];
        return logs.map((l: any) => this.mapLog(l));
      })
    );
  }

  getTodayLog(childId: string): Observable<DailyLog | null> {
    return this.http.get<any>(`${this.apiUrl}/daily-logs/child/${childId}/today`).pipe(
      map(response => response.log ? this.mapLog(response.log) : null)
    );
  }

  private mapLog(l: any): DailyLog {
    return {
      id: l.id || '',
      childId: l.childId || '',
      nurseryId: l.nurseryId || '',
      educatorId: l.educatorId || '',
      educatorName: l.educatorName || '',
      date: l.date || '',
      mood: l.mood || 3,
      appetite: l.appetite || 3,
      energy: l.energy || 3,
      sociability: l.sociability || 3,
      concentration: l.concentration || 3,
      autonomy: l.autonomy || 3,
      sleep: l.sleep || 3,
      motorSkills: l.motorSkills || 3,
      tags: l.tags || [],
      remarks: l.remarks || '',
      isInternal: l.isInternal || false,
      aiSummary: l.aiSummary || '',
      createdAt: l.createdAt || ''
    };
  }
}
