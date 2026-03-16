import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { firstValueFrom, of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { AuthGuard } from './auth.guard';
import { AuthService } from './auth.service';

describe('AuthGuard', () => {
  let guard: AuthGuard;
  let authService: Partial<AuthService>;
  let router: Partial<Router>;

  const mockRoute = {} as ActivatedRouteSnapshot;
  const mockState = { url: '/home' } as RouterStateSnapshot;
  const loginUrlTree = {} as UrlTree;

  beforeEach(() => {
    authService = {
      hasRefreshToken: vi.fn(),
      isAccessTokenExpired: vi.fn(),
      refresh: vi.fn(),
      clearSession: vi.fn()
    };

    router = {
      createUrlTree: vi.fn().mockReturnValue(loginUrlTree),
      navigate: vi.fn()
    };

    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router }
      ]
    });

    guard = TestBed.inject(AuthGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  it('should redirect to login when no refresh token is present', () => {
    (authService.hasRefreshToken as ReturnType<typeof vi.fn>).mockReturnValue(false);

    const result = guard.canActivate(mockRoute, mockState);

    expect(result).toBe(loginUrlTree);
    expect(router.createUrlTree).toHaveBeenCalledWith(['/login'], { queryParams: { returnUrl: '/home' } });
  });

  it('should allow navigation when access token is valid (not expired)', () => {
    (authService.hasRefreshToken as ReturnType<typeof vi.fn>).mockReturnValue(true);
    (authService.isAccessTokenExpired as ReturnType<typeof vi.fn>).mockReturnValue(false);

    const result = guard.canActivate(mockRoute, mockState);

    expect(result).toBe(true);
  });

  it('should attempt a silent refresh when access token is expired', async () => {
    (authService.hasRefreshToken as ReturnType<typeof vi.fn>).mockReturnValue(true);
    (authService.isAccessTokenExpired as ReturnType<typeof vi.fn>).mockReturnValue(true);
    (authService.refresh as ReturnType<typeof vi.fn>).mockReturnValue(
      of({ token: 'T2', refreshToken: 'R2', tokenType: 'Bearer', expiresIn: 900 })
    );

    const result$ = guard.canActivate(mockRoute, mockState) as ReturnType<typeof of>;
    const result = await firstValueFrom(result$);
    expect(result).toBe(true);
  });

  it('should redirect to login when silent refresh fails', async () => {
    (authService.hasRefreshToken as ReturnType<typeof vi.fn>).mockReturnValue(true);
    (authService.isAccessTokenExpired as ReturnType<typeof vi.fn>).mockReturnValue(true);
    (authService.refresh as ReturnType<typeof vi.fn>).mockReturnValue(
      throwError(() => new Error('Refresh token expirado'))
    );

    const result$ = guard.canActivate(mockRoute, mockState) as ReturnType<typeof of>;
    const result = await firstValueFrom(result$);
    expect(authService.clearSession).toHaveBeenCalled();
    expect(result).toBe(loginUrlTree);
  });
});
