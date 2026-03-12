import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { AppNotification } from '../models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getNotifications(userId: string): Observable<AppNotification[]> {
    return this.http.get<any>(`${this.apiUrl}/notifications/${userId}`).pipe(
      map(response => {
        const notifs = response.notifications || response || [];
        return notifs.map((n: any) => ({
          id: n.id?.toString() || '',
          userId: n.userId || n.user_id || '',
          message: n.message || '',
          type: n.type || 'info',
          isRead: n.isRead ?? n.is_read ?? false,
          createdAt: n.sentAt || n.sent_at || n.createdAt || n.created_at || ''
        }));
      })
    );
  }

  getUnreadCount(userId: string): Observable<number> {
    return this.http.get<any>(`${this.apiUrl}/notifications/${userId}/unread-count`).pipe(
      map(response => response.unreadCount || response.count || 0)
    );
  }

  markAsRead(notificationId: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/notifications/${notificationId}/read`, {});
  }

  markAllAsRead(userId: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/notifications/${userId}/read-all`, {});
  }

  deleteNotification(notificationId: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/notifications/${notificationId}`);
  }
}
