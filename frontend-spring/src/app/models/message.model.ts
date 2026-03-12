export interface Message {
  id: string;
  senderId: string;
  recipientId: string;
  content: string;
  sentAt: string;
  isRead: boolean;
  senderName?: string;
}
