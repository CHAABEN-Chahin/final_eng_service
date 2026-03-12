import { Message } from './message.model';

export interface Conversation {
  id: string;
  parentId: string;
  nurseryId: string;
  ownerId?: string;
  messages: Message[];
  lastMessageAt: string;
  unreadCount: number;
  lastMessage?: string;
  otherUserName?: string;
  nurseryName?: string;
  parentName?: string;
  ownerName?: string;
}
