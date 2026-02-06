import { Routes } from '@angular/router';
import { roleGuard } from '@core/auth/role.guard';

export const doctorRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('../features/doctor-home.component').then(m => m.DoctorHomeComponent),
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'profile',
    loadComponent: () => import('../features/doctor-profile.component').then(m => m.DoctorProfileComponent),
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'patients',
    loadComponent: () => import('../features/doctor-patients.component').then(m => m.DoctorPatientsComponent),
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'clinical-docs',
    loadComponent: () => import('../features/doctor-clinical-docs.component').then(m => m.DoctorClinicalDocsComponent),
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'prescriptions',
    loadComponent: () => import('../features/doctor-prescriptions.component').then(m => m.DoctorPrescriptionsComponent),
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'agenda',
    loadComponent: () => import('../features/doctor-agenda.component').then(m => m.DoctorAgendaComponent),
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'televisits',
    loadComponent: () => import('../features/doctor-televisits.component').then(m => m.DoctorTelevisitsComponent),
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'admissions',
    loadComponent: () => import('../features/doctor-admissions.component').then(m => m.DoctorAdmissionsComponent),
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'notifications',
    loadComponent: () => import('../features/doctor-notifications.component').then(m => m.DoctorNotificationsComponent),
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'docs',
    loadComponent: () => import('../features/doctor-docs.component').then(m => m.DoctorDocsComponent),
    canActivate: [roleGuard('ROLE_DOCTOR')]
  }
];
