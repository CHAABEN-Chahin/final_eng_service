import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ScheduleService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  updateScheduleItem(scheduleId: string, data: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/schedule/${scheduleId}`, data);
  }

  deleteScheduleItem(scheduleId: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/schedule/${scheduleId}`);
  }
}
