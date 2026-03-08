import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.scss'
})
export class Header {
  loggedIn = false;

  constructor(private auth: AuthService, private router: Router) {
    this.loggedIn = auth.isLoggedIn();
    auth.getLoggedIn$().subscribe(val => (this.loggedIn = val));
  }

  logout() {
    this.auth.logout().subscribe(() => this.router.navigate(['/login']));
  }
}
