import { Routes } from '@angular/router';
import { authGuard } from '@core/auth/auth.guard';

export const portalRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('../components/portal.component').then(m => m.PortalComponent),
    canActivate: [authGuard],
    children: [
      {
        path: 'patient',
        loadChildren: () => import('../components/patient/routes/patient.routes').then(m => m.patientRoutes)
      },
      {
        path: 'doctor',
        loadChildren: () => import('../components/doctor/routes/doctor.routes').then(m => m.doctorRoutes)
      },
      {
        path: 'admin',
        loadChildren: () => import('../components/admin/routes/admin.routes').then(m => m.adminRoutes)
      }
    ]
  }
];
