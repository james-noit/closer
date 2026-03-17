import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, of } from 'rxjs';
import { AuthenticationRequest } from './models/authentication-request.model';
import { AuthenticationResponse } from './models/authentication-response.model';
import { environment } from '../../environments/environment';

const TOKEN_KEY = 'closer_token';
const REFRESH_KEY = 'closer_refresh';
const TOKEN_EXP_KEY = 'closer_token_exp';

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
        this.storeTokens(resp.token, resp.refreshToken, resp.expiresIn);
        this.loggedIn$.next(true);
      })
    );
  }

  logout(): Observable<void> {
    const refresh = this.getRefreshToken();
    this.clearSession();
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
        this.storeTokens(resp.token, resp.refreshToken, resp.expiresIn);
      })
    );
  }

  /** Clears tokens and updates login state synchronously – does NOT call the backend. */
  clearSession(): void {
    this.clearTokens();
    this.loggedIn$.next(false);
  }

  isLoggedIn(): boolean {
    return this.loggedIn$.value;
  }

  /** Returns true when the stored access token has passed its expiry timestamp. */
  isAccessTokenExpired(): boolean {
    if (!isBrowser) return true;
    const exp = localStorage.getItem(TOKEN_EXP_KEY);
    if (!exp) return true;
    return Date.now() > Number(exp);
  }

  /** Returns true when a refresh token is present in storage. */
  hasRefreshToken(): boolean {
    if (!isBrowser) return false;
    return !!localStorage.getItem(REFRESH_KEY);
  }

  getToken(): string | null {
    if (!isBrowser) return null;
    return localStorage.getItem(TOKEN_KEY);
  }

  private getRefreshToken(): string | null {
    if (!isBrowser) return null;
    return localStorage.getItem(REFRESH_KEY);
  }

  private storeTokens(token: string, refresh: string, expiresIn?: number) {
    if (!isBrowser) return;
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(REFRESH_KEY, refresh);
    if (expiresIn !== null && expiresIn !== undefined) {
      localStorage.setItem(TOKEN_EXP_KEY, String(Date.now() + expiresIn * 1000));
    }
  }

  private clearTokens() {
    if (!isBrowser) return;
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
    localStorage.removeItem(TOKEN_EXP_KEY);
  }

  getLoggedIn$(): Observable<boolean> {
    return this.loggedIn$.asObservable();
  }

  getUserRole(): string | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.role ?? null;
    } catch {
      return null;
    }
  }

  isAdmin(): boolean {
    return this.getUserRole() === 'ROLE_ADMIN';
  }

  private hasToken(): boolean {
    // en SSR devolvemos false; el BehaviorSubject se sincronizará en el navegador
    return !!this.getToken();
  }
}
