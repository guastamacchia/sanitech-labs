import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const roleGuard = (role: string): CanActivateFn => {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (!auth.isAuthenticated) {
      router.navigate(['/']);
      return false;
    }
    if (!auth.hasRole(role)) {
      router.navigate(['/portal']);
      return false;
    }
    return true;
  };
};
