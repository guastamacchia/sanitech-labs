import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  if (req.url.startsWith(environment.gatewayUrl) && auth.accessToken) {
    return next(req.clone({
      setHeaders: {
        Authorization: `Bearer ${auth.accessToken}`
      }
    }));
  }
  return next(req);
};
