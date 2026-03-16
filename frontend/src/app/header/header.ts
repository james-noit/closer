import { Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.scss'
})
export class Header {
  private readonly destroyRef = inject(DestroyRef);

  loggedIn = false;
  showNavItems = true;
  isMenuOpen = false;

  constructor(private auth: AuthService, private router: Router) {
    this.loggedIn = auth.isLoggedIn();
    auth
      .getLoggedIn$()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(val => (this.loggedIn = val));

    this.showNavItems = this.shouldShowNavItems(this.router.url);

    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((event: NavigationEnd) => {
        this.showNavItems = this.shouldShowNavItems(event.urlAfterRedirects);
        this.closeMenu();
      });
  }

  logout() {
    this.auth.logout().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.closeMenu();
      void this.router.navigate(['/login']);
    });
  }

  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen;
  }

  closeMenu() {
    this.isMenuOpen = false;
  }

  private shouldShowNavItems(url: string): boolean {
    return !url.startsWith('/login');
  }
}
