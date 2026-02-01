import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  let request = req;

  if (req.url.startsWith(environment.gatewayUrl) && auth.accessToken) {
    request = req.clone({
      setHeaders: {
        Authorization: `Bearer ${auth.accessToken}`
      }
    });
  }

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && auth.isAuthenticated) {
        auth.logout();
      }
      return throwError(() => error);
    })
  );
};
