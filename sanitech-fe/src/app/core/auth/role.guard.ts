import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const roleGuard = (role: string): CanActivateFn => {
  return async () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    // Se il token non è ancora valido, tenta il refresh (gestisce il caso
    // di page-refresh / deep-link dove l'APP_INITIALIZER ha caricato il
    // discovery document ma il token potrebbe non essere ancora disponibile)
    let authenticated = auth.isAuthenticated;
    if (!authenticated) {
      authenticated = await auth.tryRefreshLogin();
    }

    if (!authenticated) {
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
