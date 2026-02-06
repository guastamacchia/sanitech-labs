import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadChildren: () => import('./public-portal/routes/public.routes')
      .then(m => m.publicRoutes)
  },
  {
    path: 'portal',
    loadChildren: () => import('./private-portal/private.routes')
      .then(m => m.portalRoutes)
  },
  {
    path: 'televisit',
    loadChildren: () => import('./private-portal/televisit/televisit.routes')
      .then(m => m.televisitRoutes)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
