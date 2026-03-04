import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from '../../../services/message.service';
import { Conversation } from '../../../models/message.model';

@Component({
  selector: 'app-conversations-list',
  templateUrl: './conversations-list.component.html',
  styleUrls: ['./conversations-list.component.scss']
})
export class ConversationsListComponent implements OnInit {
  conversations: Conversation[] = [];
  loading = true;

  constructor(private messageService: MessageService, private router: Router) {}

  ngOnInit(): void {
    this.messageService.getConversations().subscribe({
      next: (convs) => { this.conversations = convs; this.loading = false; },
      error: () => {
        this.loading = false;
        this.conversations = [
          { contactId: 1, contactName: 'Alice Johnson', lastMessage: 'Hey! How are you?', lastMessageAt: new Date().toISOString(), unreadCount: 2 },
          { contactId: 2, contactName: 'Bob Smith', lastMessage: "Let's catch up soon!", lastMessageAt: new Date(Date.now() - 86400000).toISOString(), unreadCount: 0 }
        ];
      }
    });
  }

  openConversation(contactId: number): void {
    this.router.navigate(['/messages', contactId]);
  }
}
