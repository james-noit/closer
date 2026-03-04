export interface Message {
  id: number;
  senderId: number;
  receiverId: number;
  content: string;
  read: boolean;
  createdAt: string;
}

export interface Conversation {
  contactId: number;
  contactName: string;
  lastMessage?: string;
  lastMessageAt?: string;
  unreadCount: number;
}
