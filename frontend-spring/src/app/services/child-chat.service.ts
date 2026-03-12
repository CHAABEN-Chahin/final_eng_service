import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ChatMessageDto } from '../models/chat-message.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ChildChatService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  sendMessage(childId: string, userId: string, message: string): Observable<{ reply: string; queryType: string }> {
    return this.http.post<any>(`${this.apiUrl}/child-chat`, {
      childId, userId, message
    }).pipe(
      map(res => ({
        reply: res.reply || '',
        queryType: res.queryType || 'unknown'
      }))
    );
  }

  getHistory(childId: string, userId: string): Observable<ChatMessageDto[]> {
    return this.http.get<any>(`${this.apiUrl}/child-chat/${childId}/${userId}/history`).pipe(
      map(res => (res.messages || []) as ChatMessageDto[])
    );
  }
}
