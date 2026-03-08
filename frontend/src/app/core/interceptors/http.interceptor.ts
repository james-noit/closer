import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../../auth/auth.service';

/**
 * Interceptor HTTP centralizado.
 * - Adjunta el token JWT a cada petición saliente.
 * - Intenta un refresh transparente ante un 401.
 * - Propaga errores al caller para que cada componente los gestione
 *   (toast genérico, tarjeta de error en login, etc.).
 */
export const httpInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);

  // No añadimos token a las peticiones de auth (login/refresh/logout)
  const isAuthUrl = req.url.includes('/auth/');
  const token = auth.getToken();

  const authReq = (!isAuthUrl && token)
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError(err => {
      if (err instanceof HttpErrorResponse && err.status === 401 && !isAuthUrl) {
        return handle401(auth, req, next);
      }
      return throwError(() => err);
    })
  );
};

function handle401(auth: AuthService, req: Parameters<HttpInterceptorFn>[0], next: Parameters<HttpInterceptorFn>[1]) {
  return auth.refresh().pipe(
    switchMap(resp => {
      const cloned = req.clone({
        setHeaders: { Authorization: `Bearer ${resp.token}` }
      });
      return next(cloned);
    }),
    catchError(refreshErr => {
      auth.logout();
      return throwError(() => refreshErr);
    })
  );
}
