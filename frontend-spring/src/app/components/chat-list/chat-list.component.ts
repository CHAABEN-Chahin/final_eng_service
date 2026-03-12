import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ConversationService } from '../../services/conversation.service';
import { Conversation } from '../../models';

@Component({
  selector: 'app-chat-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './chat-list.component.html',
  styleUrl: './chat-list.component.scss'
})
export class ChatListComponent implements OnInit {
  conversations: Conversation[] = [];
  isLoading = true;

  constructor(
    private authService: AuthService,
    private conversationService: ConversationService,
    private router: Router
  ) {}

  ngOnInit() {
    const user = this.authService.currentUser;
    if (user) {
      this.conversationService.getConversations(user.id).subscribe({
        next: (conversations) => {
          this.conversations = conversations;
          this.isLoading = false;
        },
        error: () => this.isLoading = false
      });
    }
  }

  openChat(conversationId: string) {
    this.router.navigate(['/chat', conversationId]);
  }

  getTimeAgo(dateStr: string): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    if (minutes < 1) return "À l'instant";
    if (minutes < 60) return `${minutes}min`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h`;
    const days = Math.floor(hours / 24);
    return `${days}j`;
  }
}
