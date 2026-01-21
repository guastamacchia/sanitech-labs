import { Routes } from '@angular/router';
import { PublicPageComponent } from './features/public/public-page.component';
import { privateRoutes } from './features/private/private.routes';

export const routes: Routes = [
  {
    path: '',
    component: PublicPageComponent
  },
  ...privateRoutes,
  {
    path: '**',
    redirectTo: ''
  }
];
