import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { Reminder } from '../models/reminder.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ReminderService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getReminders(): Observable<Reminder[]> {
    return this.http.get<Reminder[]>(`${this.apiUrl}/reminders`).pipe(
      catchError(err => throwError(() => err))
    );
  }

  createReminder(reminder: Partial<Reminder>): Observable<Reminder> {
    return this.http.post<Reminder>(`${this.apiUrl}/reminders`, reminder).pipe(
      catchError(err => throwError(() => err))
    );
  }

  updateReminder(id: number, reminder: Partial<Reminder>): Observable<Reminder> {
    return this.http.put<Reminder>(`${this.apiUrl}/reminders/${id}`, reminder).pipe(
      catchError(err => throwError(() => err))
    );
  }

  deleteReminder(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/reminders/${id}`).pipe(
      catchError(err => throwError(() => err))
    );
  }
}
