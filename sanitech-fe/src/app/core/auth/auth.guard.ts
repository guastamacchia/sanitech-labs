import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  // Tenta il refresh se il token è scaduto ma il refresh token è disponibile
  const isValid = await auth.tryRefreshLogin();

  if (!isValid) {
    router.navigate(['/']);
    return false;
  }

  return true;
};
