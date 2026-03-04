import { Component, OnInit, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MessageService } from '../../../services/message.service';
import { AuthService } from '../../../services/auth.service';
import { Message } from '../../../models/message.model';

@Component({
  selector: 'app-conversation',
  templateUrl: './conversation.component.html',
  styleUrls: ['./conversation.component.scss']
})
export class ConversationComponent implements OnInit, AfterViewChecked {
  @ViewChild('messagesEnd') private messagesEnd!: ElementRef;
  messages: Message[] = [];
  newMessage = '';
  loading = true;
  contactId!: number;
  currentUserId: number = 1;

  constructor(
    private route: ActivatedRoute,
    private messageService: MessageService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.contactId = parseInt(this.route.snapshot.paramMap.get('contactId') || '0');
    const user = this.authService.getCurrentUser();
    if (user) this.currentUserId = user.id;
    this.loadMessages();
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  loadMessages(): void {
    this.messageService.getConversation(this.contactId).subscribe({
      next: (msgs) => { this.messages = msgs; this.loading = false; },
      error: () => {
        this.loading = false;
        this.messages = [
          { id: 1, senderId: this.contactId, receiverId: this.currentUserId, content: 'Hey! How are you doing?', read: true, createdAt: new Date(Date.now() - 3600000).toISOString() },
          { id: 2, senderId: this.currentUserId, receiverId: this.contactId, content: "Hi! I'm doing great, thanks for asking!", read: true, createdAt: new Date(Date.now() - 1800000).toISOString() }
        ];
      }
    });
  }

  sendMessage(): void {
    if (!this.newMessage.trim()) return;
    const content = this.newMessage.trim();
    this.newMessage = '';
    this.messageService.sendMessage(this.contactId, content).subscribe({
      next: (msg) => this.messages.push(msg),
      error: () => {
        this.messages.push({
          id: Date.now(), senderId: this.currentUserId, receiverId: this.contactId,
          content, read: false, createdAt: new Date().toISOString()
        });
      }
    });
  }

  isMyMessage(msg: Message): boolean {
    return msg.senderId === this.currentUserId;
  }

  private scrollToBottom(): void {
    try { this.messagesEnd?.nativeElement?.scrollIntoView({ behavior: 'smooth' }); } catch {}
  }
}
