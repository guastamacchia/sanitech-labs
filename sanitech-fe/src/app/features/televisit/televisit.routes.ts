import { Routes } from '@angular/router';
import { TelevisitRoomComponent } from './televisit-room.component';
import { authGuard } from '../../core/auth/auth.guard';

export const televisitRoutes: Routes = [
  {
    path: 'televisit/room',
    component: TelevisitRoomComponent,
    canActivate: [authGuard]
  }
];
