import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable, of, catchError, map } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | boolean | UrlTree {
    const loginTree = this.router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });

    if (!this.auth.hasRefreshToken()) {
      // No session at all → redirect to login
      return loginTree;
    }

    if (!this.auth.isAccessTokenExpired()) {
      // Valid, non-expired access token → allow navigation
      return true;
    }

    // Access token has expired – attempt a silent refresh before deciding.
    return this.auth.refresh().pipe(
      map(() => true),
      catchError(() => {
        this.auth.clearSession();
        return of(loginTree);
      })
    );
  }
}
