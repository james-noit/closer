import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { Message, Conversation } from '../models/message.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MessageService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getConversations(): Observable<Conversation[]> {
    return this.http.get<Conversation[]>(`${this.apiUrl}/messages/conversations`).pipe(
      catchError(err => throwError(() => err))
    );
  }

  getConversation(contactId: number): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.apiUrl}/messages/conversations/${contactId}`).pipe(
      catchError(err => throwError(() => err))
    );
  }

  sendMessage(contactId: number, content: string): Observable<Message> {
    return this.http.post<Message>(`${this.apiUrl}/messages`, { receiverId: contactId, content }).pipe(
      catchError(err => throwError(() => err))
    );
  }

  markAsRead(contactId: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/messages/conversations/${contactId}/read`, {}).pipe(
      catchError(err => throwError(() => err))
    );
  }
}
