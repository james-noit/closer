import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, of } from 'rxjs';
import { AuthenticationRequest } from './models/authentication-request.model';
import { AuthenticationResponse } from './models/authentication-response.model';
import { environment } from '../../environments/environment';

const TOKEN_KEY = 'closer_token';
const REFRESH_KEY = 'closer_refresh';

// util para verificar si estamos en el navegador (no en SSR)
const isBrowser = typeof window !== 'undefined' && typeof window.localStorage !== 'undefined';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private loggedIn$ = new BehaviorSubject<boolean>(this.hasToken());

  constructor(private http: HttpClient) {}

  private base = environment.apiUrl;

  login(username: string, password: string): Observable<AuthenticationResponse> {
    const req: AuthenticationRequest = { username, password };
    return this.http.post<AuthenticationResponse>(`${this.base}/auth/login`, req).pipe(
      tap(resp => {
        this.storeTokens(resp.token, resp.refreshToken);
        this.loggedIn$.next(true);
      })
    );
  }

  logout(): Observable<void> {
    const refresh = this.getRefreshToken();
    this.clearTokens();
    this.loggedIn$.next(false);
    if (refresh) {
      return this.http.post<void>(`${this.base}/auth/logout`, { refreshToken: refresh });
    }
    return of();
  }

  refresh(): Observable<AuthenticationResponse> {
    const refresh = this.getRefreshToken();
    if (!refresh) {
      throw new Error('No refresh token available');
    }
    return this.http.post<AuthenticationResponse>(`${this.base}/auth/refresh`, { refreshToken: refresh }).pipe(
      tap(resp => {
        this.storeTokens(resp.token, resp.refreshToken);
      })
    );
  }

  isLoggedIn(): boolean {
    return this.loggedIn$.value;
  }

  getToken(): string | null {
    if (!isBrowser) return null;
    return localStorage.getItem(TOKEN_KEY);
  }

  private getRefreshToken(): string | null {
    if (!isBrowser) return null;
    return localStorage.getItem(REFRESH_KEY);
  }

  private storeTokens(token: string, refresh: string) {
    if (!isBrowser) return;
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(REFRESH_KEY, refresh);
  }

  private clearTokens() {
    if (!isBrowser) return;
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
  }

  getLoggedIn$(): Observable<boolean> {
    return this.loggedIn$.asObservable();
  }

  private hasToken(): boolean {
    // en SSR devolvemos false; el BehaviorSubject se sincronizará en el navegador
    return !!this.getToken();
  }
}
