import { Component, OnInit, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ChildChatService } from '../../services/child-chat.service';
import { ChatMessageDto } from '../../models';

interface DisplayMessage {
  role: 'user' | 'assistant' | 'system';
  content: string;
  isTyping?: boolean;
  createdAt?: string;
}

@Component({
  selector: 'app-child-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './child-chat.component.html',
  styleUrl: './child-chat.component.scss'
})
export class ChildChatComponent implements OnInit, AfterViewChecked {
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  childId = '';
  childName = '';
  userId = '';
  messages: DisplayMessage[] = [];
  newMessage = '';
  isLoading = true;
  isSending = false;
  private shouldScroll = false;

  suggestedQuestions = [
    'Comment va mon enfant en ce moment ?',
    'Est-ce qu\'il dort bien ces derniers jours ?',
    'Comment évolue son appétit ?',
    'Est-il sociable avec les autres enfants ?',
    'Y a-t-il des moments marquants récemment ?',
    'Comment est sa concentration pendant les activités ?'
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private chatService: ChildChatService
  ) {}

  ngOnInit() {
    this.childId = this.route.snapshot.paramMap.get('childId') || '';
    this.childName = history.state?.childName || sessionStorage.getItem('currentChildName') || 'Mon enfant';

    const user = this.authService.currentUser;
    if (!user || !this.childId) {
      this.router.navigate(['/welcome']);
      return;
    }
    this.userId = user.id;

    this.loadHistory();
  }

  ngAfterViewChecked() {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  loadHistory() {
    this.chatService.getHistory(this.childId, this.userId).subscribe({
      next: (msgs) => {
        this.messages = msgs.map(m => ({
          role: m.role,
          content: m.content,
          createdAt: m.createdAt
        }));

        // Add welcome message if empty
        if (this.messages.length === 0) {
          this.messages.push({
            role: 'assistant',
            content: `Bonjour ! 👋 Je suis l'assistant intelligent de ${this.childName}. ` +
              `Je connais toutes ses observations quotidiennes à la garderie. ` +
              `Posez-moi n'importe quelle question sur son humeur, son appétit, son sommeil, ` +
              `ses interactions sociales, ou son évolution générale !`
          });
        }

        this.isLoading = false;
        this.shouldScroll = true;
      },
      error: () => {
        this.messages.push({
          role: 'assistant',
          content: `Bonjour ! 👋 Je suis l'assistant de ${this.childName}. Comment puis-je vous aider ?`
        });
        this.isLoading = false;
      }
    });
  }

  sendMessage(text?: string) {
    const message = (text || this.newMessage).trim();
    if (!message || this.isSending) return;

    // Add user message
    this.messages.push({ role: 'user', content: message });
    this.newMessage = '';
    this.isSending = true;
    this.shouldScroll = true;

    // Add typing indicator
    this.messages.push({ role: 'assistant', content: '', isTyping: true });

    this.chatService.sendMessage(this.childId, this.userId, message).subscribe({
      next: (res) => {
        // Remove typing indicator
        this.messages = this.messages.filter(m => !m.isTyping);
        // Add assistant response
        this.messages.push({ role: 'assistant', content: res.reply });
        this.isSending = false;
        this.shouldScroll = true;
      },
      error: () => {
        this.messages = this.messages.filter(m => !m.isTyping);
        this.messages.push({
          role: 'assistant',
          content: 'Désolé, je n\'ai pas pu traiter votre demande. Veuillez réessayer.'
        });
        this.isSending = false;
        this.shouldScroll = true;
      }
    });
  }

  onKeyDown(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  private scrollToBottom() {
    try {
      const el = this.messagesContainer?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    } catch (e) {}
  }

  get showSuggestions(): boolean {
    return this.messages.length <= 1 && !this.isSending;
  }

  goBack() {
    const user = this.authService.currentUser;
    if (user?.type === 'nursery') {
      this.router.navigate(['/nursery/espace-enfant', this.childId]);
    } else {
      this.router.navigate(['/parent/espace-enfant', this.childId]);
    }
  }
}
