import { Component, OnInit, OnDestroy, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ConversationService } from '../../services/conversation.service';
import { NurseryService } from '../../services/nursery.service';
import { Message } from '../../models';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.scss'
})
export class ChatComponent implements OnInit, OnDestroy {
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  messages: Message[] = [];
  newMessage = '';
  conversationId = '';
  recipientId = '';
  currentUserId = '';
  parentId = '';
  nurseryId = '';
  ownerId = '';
  isLoading = true;
  private refreshInterval: ReturnType<typeof setInterval> | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    public authService: AuthService,
    private conversationService: ConversationService,
    private nurseryService: NurseryService
  ) {}

  ngOnInit() {
    this.currentUserId = this.authService.currentUser?.id || '';
    this.conversationId = this.route.snapshot.paramMap.get('conversationId') || '';
    
    // Check if recipient ID is stored in session storage
    this.recipientId = sessionStorage.getItem('chatRecipientId') || '';
    this.parentId = sessionStorage.getItem('chatParentId') || '';
    this.nurseryId = sessionStorage.getItem('chatNurseryId') || '';
    
    if (this.conversationId) {
      this.loadMessages();
      
      // If we don't have recipient ID but have nursery ID, fetch it
      if (!this.recipientId && this.nurseryId) {
        this.nurseryService.getNurseryById(this.nurseryId).subscribe({
          next: (nursery) => {
            if (nursery.ownerId) {
              this.recipientId = nursery.ownerId;
              this.ownerId = nursery.ownerId;
            }
          },
          error: (err) => console.error('Error fetching nursery:', err)
        });
      }
      
      this.refreshInterval = setInterval(() => this.loadMessages(), 5000);
    }
  }

  ngOnDestroy() {
    if (this.refreshInterval) clearInterval(this.refreshInterval);
  }

  loadMessages() {
    this.conversationService.getMessages(this.conversationId).subscribe({
      next: (response: any) => {
        // Handle response object with messages array and metadata
        if (response && response.messages) {
          this.messages = response.messages.map((m: any) => this.mapMessage(m));
          // Extract participant IDs from response
          this.parentId = response.parentId || '';
          this.ownerId = response.ownerId || '';
        } else if (Array.isArray(response)) {
          // Fallback: if response is directly an array
          this.messages = response.map((m: any) => this.mapMessage(m));
        }

        // Determine recipient ID
        if (!this.recipientId) {
          if (this.ownerId && this.parentId && this.currentUserId) {
            // We have all IDs, determine which one is the recipient
            if (this.currentUserId === this.parentId) {
              // Current user is parent, recipient is owner
              this.recipientId = this.ownerId;
            } else {
              // Current user is owner, recipient is parent
              this.recipientId = this.parentId;
            }
          } else if (this.messages.length > 0) {
            // Fallback: find recipient from messages
            const otherUserMessage = this.messages.find(m => m.senderId !== this.currentUserId);
            if (otherUserMessage) {
              this.recipientId = otherUserMessage.senderId;
            } else {
              const myMessage = this.messages.find(m => m.senderId === this.currentUserId);
              if (myMessage?.recipientId) {
                this.recipientId = myMessage.recipientId;
              }
            }
          }
        }

        this.isLoading = false;
        setTimeout(() => this.scrollToBottom(), 100);
      },
      error: (err) => {
        console.error('Error loading messages:', err);
        this.isLoading = false;
      }
    });

    const user = this.authService.currentUser;
    if (user) {
      this.conversationService.markAsRead(this.conversationId, user.id).subscribe();
    }
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

  sendMessage() {
    if (!this.newMessage.trim()) return;
    const user = this.authService.currentUser;
    if (!user) {
      alert('You must be logged in to send messages');
      return;
    }

    // Make sure we have a recipient ID
    if (!this.recipientId) {
      // Try to get from session storage one more time
      this.recipientId = sessionStorage.getItem('chatRecipientId') || '';
      
      // If still no recipient, try to find from messages
      if (!this.recipientId && this.messages.length > 0) {
        const otherUserMessage = this.messages.find(m => m.senderId !== this.currentUserId);
        if (otherUserMessage) {
          this.recipientId = otherUserMessage.senderId;
        } else {
          const myMessage = this.messages.find(m => m.senderId === this.currentUserId);
          if (myMessage?.recipientId) {
            this.recipientId = myMessage.recipientId;
          }
        }
      }
    }

    if (!this.recipientId) {
      alert('Unable to determine recipient. Please refresh the page and try again.');
      return;
    }

    this.conversationService.sendMessage(this.conversationId, user.id, this.recipientId, this.newMessage).subscribe({
      next: () => {
        this.newMessage = '';
        this.loadMessages();
      },
      error: (err) => {
        console.error('Error sending message:', err);
        alert('Error sending message: ' + (err.error?.message || 'Please try again'));
      }
    });
  }

  scrollToBottom() {
    if (this.messagesContainer) {
      const el = this.messagesContainer.nativeElement;
      el.scrollTop = el.scrollHeight;
    }
  }

  isMyMessage(message: Message): boolean {
    return message.senderId === this.currentUserId;
  }

  goBack() { this.router.navigate(['/chat']); }
}
