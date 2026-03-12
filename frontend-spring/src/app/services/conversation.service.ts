import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Conversation, Message } from '../models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ConversationService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getOrCreateConversation(parentId: string, nurseryOwnerId: string, nurseryId: string): Observable<Conversation> {
    return this.http.post<any>(`${this.apiUrl}/conversations/get-or-create`, {
      parentId,
      nurseryId
    }).pipe(map(r => this.mapConversation(r.conversation || r)));
  }

  getConversations(userId: string): Observable<Conversation[]> {
    return this.http.get<any>(`${this.apiUrl}/conversations/user/${userId}`).pipe(
      map(response => {
        const conversations = response.conversations || response || [];
        return conversations.map((c: any) => this.mapConversation(c));
      })
    );
  }

  getMessages(conversationId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/conversations/${conversationId}/messages`);
  }

  getConversation(conversationId: string): Observable<Conversation> {
    return this.getConversations('').pipe(
      map(conversations => {
        // This is a workaround - ideally we'd have a single endpoint
        // For now, return empty conversation object with just ID
        return {
          id: conversationId,
          parentId: '',
          nurseryId: '',
          ownerId: '',
          messages: [],
          lastMessageAt: new Date().toISOString(),
          unreadCount: 0,
          lastMessage: '',
          otherUserName: '',
          nurseryName: '',
          parentName: '',
          ownerName: ''
        };
      })
    );
  }

  sendMessage(conversationId: string, senderId: string, recipientId: string, content: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/conversations/${conversationId}/messages`, {
      senderId,
      recipientId,
      content
    });
  }

  markAsRead(conversationId: string, userId: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/conversations/${conversationId}/mark-read`, { userId });
  }

  private mapConversation(c: any): Conversation {
    return {
      id: c.id?.toString() || '',
      parentId: c.parentId || c.parent_id || '',
      nurseryId: c.nurseryId || c.nursery_id || c.garderieId || '',
      ownerId: c.ownerId || c.owner_id || '',
      messages: (c.messages || []).map((m: any) => this.mapMessage(m)),
      lastMessageAt: c.lastMessageAt || c.last_message_at || new Date().toISOString(),
      unreadCount: parseInt(c.unreadCount || c.messages_non_lus || c.messagesNonLus) || 0,
      lastMessage: c.lastMessage || c.last_message || '',
      otherUserName: c.otherUserName || c.other_user_name || '',
      nurseryName: c.nurseryName || c.nursery_name || '',
      parentName: c.parentName || c.parent_name || '',
      ownerName: c.ownerName || c.owner_name || ''
    };
  }

  private mapMessage(m: any): Message {
    return {
      id: m.id?.toString() || '',
      senderId: m.senderId || m.sender_id || m.expediteurId || '',
      recipientId: m.recipientId || m.recipient_id || m.destinataireId || '',
      content: m.content || m.contenu || '',
      sentAt: m.sentAt || m.sent_at || m.dateEnvoi || new Date().toISOString(),
      isRead: m.isRead ?? m.is_read ?? m.estLu ?? false,
      senderName: m.senderName || m.sender_name || ''
    };
  }
}
