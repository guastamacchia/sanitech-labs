import { Routes } from '@angular/router';
import { authGuard } from '@core/auth/auth.guard';

export const televisitRoutes: Routes = [
  {
    path: 'room',
    loadComponent: () => import('./televisit-room.component').then(m => m.TelevisitRoomComponent),
    canActivate: [authGuard]
  }
];
