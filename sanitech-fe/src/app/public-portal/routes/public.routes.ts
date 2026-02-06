import { Routes } from '@angular/router';

// Definizione route portale pubblico
export const publicRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('../components/public-root/public-root.component')
      .then(m => m.PublicRootComponent)
  }
];
