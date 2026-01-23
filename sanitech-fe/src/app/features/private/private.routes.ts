import { Routes } from '@angular/router';
import { authGuard } from '../../core/auth/auth.guard';
import { PortalComponent } from './portal/portal.component';
import { adminRoutes } from './admin/admin.routes';
import { doctorRoutes } from './doctor/doctor.routes';
import { patientRoutes } from './patient/patient.routes';

export const privateRoutes: Routes = [
  {
    path: 'portal',
    component: PortalComponent,
    canActivate: [authGuard],
    children: [...patientRoutes, ...doctorRoutes, ...adminRoutes]
  }
];
