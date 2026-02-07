import { Routes } from '@angular/router';
import { roleGuard } from '@core/auth/role.guard';

export const doctorRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('../components/doctor-home.component').then(m => m.DoctorHomeComponent),
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'profile',
    loadComponent: () => import('../components/sections/profile/doctor-profile.component').then(m => m.DoctorProfileComponent),
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'patients',
    loadComponent: () => import('../components/sections/patients/doctor-patients.component').then(m => m.DoctorPatientsComponent),
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'clinical-docs',
    loadComponent: () => import('../components/sections/clinical-docs/doctor-clinical-docs.component').then(m => m.DoctorClinicalDocsComponent),
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'prescriptions',
    loadComponent: () => import('../components/sections/prescriptions/doctor-prescriptions.component').then(m => m.DoctorPrescriptionsComponent),
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'agenda',
    loadComponent: () => import('../components/sections/agenda/doctor-agenda.component').then(m => m.DoctorAgendaComponent),
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'televisits',
    loadComponent: () => import('../components/sections/televisits/doctor-televisits.component').then(m => m.DoctorTelevisitsComponent),
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'admissions',
    loadComponent: () => import('../components/sections/admissions/doctor-admissions.component').then(m => m.DoctorAdmissionsComponent),
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'notifications',
    loadComponent: () => import('../components/sections/notifications/doctor-notifications.component').then(m => m.DoctorNotificationsComponent),
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'docs',
    loadComponent: () => import('../components/sections/docs/doctor-docs.component').then(m => m.DoctorDocsComponent),
    canActivate: [roleGuard('ROLE_DOCTOR')]
  }
];
