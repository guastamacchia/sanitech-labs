import { HttpInterceptorFn, HttpErrorResponse, HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError, from, Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

let isRefreshing = false;

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);

  // Skip non-gateway requests
  if (!req.url.startsWith(environment.gatewayUrl)) {
    return next(req);
  }

  // Add token if available
  const request = addToken(req, auth.accessToken);

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && auth.isAuthenticated && !isRefreshing) {
        return handleUnauthorized(req, next, auth);
      }
      return throwError(() => error);
    })
  );
};

function addToken(req: HttpRequest<unknown>, token: string | null): HttpRequest<unknown> {
  if (token) {
    return req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }
  return req;
}

function handleUnauthorized(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
  auth: AuthService
): Observable<HttpEvent<unknown>> {
  isRefreshing = true;

  return from(auth.refreshToken()).pipe(
    switchMap((success) => {
      isRefreshing = false;
      if (success) {
        // Retry request with new token
        const retryReq = addToken(req, auth.accessToken);
        return next(retryReq);
      } else {
        // Refresh failed, logout
        auth.logout();
        return throwError(() => new HttpErrorResponse({ status: 401, statusText: 'Unauthorized' }));
      }
    }),
    catchError((err) => {
      isRefreshing = false;
      auth.logout();
      return throwError(() => err);
    })
  );
}
