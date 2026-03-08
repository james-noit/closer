import { Component, NgZone, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  form: FormGroup;
  readonly loading = signal(false);
  readonly errorTitle = signal<string | null>(null);
  readonly errorDetail = signal<string | null>(null);
  private returnUrl: string | null = null;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private ngZone: NgZone
  ) {
    this.form = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });

    if (this.auth.isLoggedIn()) {
      this.router.navigate(['/']);
    }

    this.returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
  }

  submit() {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.errorTitle.set(null);
    this.errorDetail.set(null);

    const { username, password } = this.form.value;
    this.auth.login(username!, password!).subscribe({
      next: () => {
        this.ngZone.run(() => {
          this.loading.set(false);
          const target = this.returnUrl || '/';
          this.router.navigateByUrl(target);
        });
      },
      error: (err: HttpErrorResponse) => {
        this.ngZone.run(() => {
          this.loading.set(false);
          this.errorTitle.set(err.error?.title || 'Error al iniciar sesión');
          this.errorDetail.set(err.error?.detail || null);
        });
      }
    });
  }
}
