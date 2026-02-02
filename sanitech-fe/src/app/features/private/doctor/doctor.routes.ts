import { Routes } from '@angular/router';
import { roleGuard } from '../../../core/auth/role.guard';
import { DoctorHomeComponent } from './doctor-home.component';
import { DoctorProfileComponent } from './doctor-profile.component';
import { DoctorPatientsComponent } from './doctor-patients.component';
import { DoctorClinicalDocsComponent } from './doctor-clinical-docs.component';
import { DoctorPrescriptionsComponent } from './doctor-prescriptions.component';
import { DoctorAgendaComponent } from './doctor-agenda.component';
import { DoctorTelevisitsComponent } from './doctor-televisits.component';
import { DoctorAdmissionsComponent } from './doctor-admissions.component';
import { DoctorNotificationsComponent } from './doctor-notifications.component';
import { DoctorDocsComponent } from './doctor-docs.component';

export const doctorRoutes: Routes = [
  {
    path: 'doctor',
    component: DoctorHomeComponent,
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'doctor/profile',
    component: DoctorProfileComponent,
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'doctor/patients',
    component: DoctorPatientsComponent,
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'doctor/clinical-docs',
    component: DoctorClinicalDocsComponent,
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'doctor/prescriptions',
    component: DoctorPrescriptionsComponent,
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'doctor/agenda',
    component: DoctorAgendaComponent,
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'doctor/televisits',
    component: DoctorTelevisitsComponent,
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'doctor/admissions',
    component: DoctorAdmissionsComponent,
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'doctor/notifications',
    component: DoctorNotificationsComponent,
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'doctor/docs',
    component: DoctorDocsComponent,
    canActivate: [roleGuard('ROLE_DOCTOR')]
  }
];
