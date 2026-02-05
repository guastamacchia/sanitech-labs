import { Routes } from '@angular/router';
import { PublicPageComponent } from './features/public/public-page.component';
import { privateRoutes } from './features/private/private.routes';
import { televisitRoutes } from './features/televisit/televisit.routes';

export const routes: Routes = [
  {
    path: '',
    component: PublicPageComponent
  },
  ...privateRoutes,
  ...televisitRoutes,
  {
    path: '**',
    redirectTo: ''
  }
];
