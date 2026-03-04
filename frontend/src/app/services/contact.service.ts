import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { Contact } from '../models/contact.model';
import { DashboardData } from '../models/dashboard.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ContactService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getContacts(group?: number): Observable<Contact[]> {
    let params = new HttpParams();
    if (group) params = params.set('group', group.toString());
    return this.http.get<Contact[]>(`${this.apiUrl}/contacts`, { params }).pipe(
      catchError(err => throwError(() => err))
    );
  }

  getDashboard(): Observable<DashboardData> {
    return this.http.get<DashboardData>(`${this.apiUrl}/contacts/dashboard`).pipe(
      catchError(err => throwError(() => err))
    );
  }

  getContact(id: number): Observable<Contact> {
    return this.http.get<Contact>(`${this.apiUrl}/contacts/${id}`).pipe(
      catchError(err => throwError(() => err))
    );
  }

  addContact(contact: Partial<Contact>): Observable<Contact> {
    return this.http.post<Contact>(`${this.apiUrl}/contacts`, contact).pipe(
      catchError(err => throwError(() => err))
    );
  }

  updateContact(id: number, contact: Partial<Contact>): Observable<Contact> {
    return this.http.put<Contact>(`${this.apiUrl}/contacts/${id}`, contact).pipe(
      catchError(err => throwError(() => err))
    );
  }

  deleteContact(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/contacts/${id}`).pipe(
      catchError(err => throwError(() => err))
    );
  }

  searchContacts(query: string): Observable<Contact[]> {
    const params = new HttpParams().set('q', query);
    return this.http.get<Contact[]>(`${this.apiUrl}/contacts/search`, { params }).pipe(
      catchError(err => throwError(() => err))
    );
  }

  syncContacts(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/contacts/sync`, {}).pipe(
      catchError(err => throwError(() => err))
    );
  }

  updateInteraction(id: number): Observable<Contact> {
    return this.http.post<Contact>(`${this.apiUrl}/contacts/${id}/interact`, {}).pipe(
      catchError(err => throwError(() => err))
    );
  }

  importContacts(data: string): Observable<Contact[]> {
    return this.http.post<Contact[]>(`${this.apiUrl}/contacts/import`, { data }).pipe(
      catchError(err => throwError(() => err))
    );
  }
}
