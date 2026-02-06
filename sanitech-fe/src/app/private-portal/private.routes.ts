import { Routes } from '@angular/router';
import { authGuard } from '@core/auth/auth.guard';

export const portalRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./shell-shared/portal.component').then(m => m.PortalComponent),
    canActivate: [authGuard],
    children: [
      {
        path: 'patient',
        loadChildren: () => import('./patient/routes/patient.routes').then(m => m.patientRoutes)
      },
      {
        path: 'doctor',
        loadChildren: () => import('./doctor/routes/doctor.routes').then(m => m.doctorRoutes)
      },
      {
        path: 'admin',
        loadChildren: () => import('./admin/routes/admin.routes').then(m => m.adminRoutes)
      }
    ]
  }
];
