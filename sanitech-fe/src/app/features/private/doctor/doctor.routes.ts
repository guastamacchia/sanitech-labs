import { Routes } from '@angular/router';
import { roleGuard } from '../../../core/auth/role.guard';
import { DoctorHomeComponent } from './doctor-home.component';
import { DoctorDocsComponent } from './doctor-docs.component';
import { ResourcePageComponent } from '../shared/resources/resource-page.component';
import {
  admissionsEndpoints,
  paymentsEndpoints,
  prescribingEndpoints,
  schedulingEndpoints,
  televisitEndpoints
} from '../shared/resources/resource-endpoints';

export const doctorRoutes: Routes = [
  {
    path: 'doctor',
    component: DoctorHomeComponent,
    canActivate: [roleGuard('ROLE_DOCTOR')]
  },
  {
    path: 'doctor/prescribing',
    component: ResourcePageComponent,
    canActivate: [roleGuard('ROLE_DOCTOR')],
    data: {
      title: 'Prescrizioni',
      description: 'Gestione prescrizioni e terapie.',
      endpoints: prescribingEndpoints,
      view: 'prescribing'
    }
  },
  {
    path: 'doctor/televisit',
    component: ResourcePageComponent,
    canActivate: [roleGuard('ROLE_DOCTOR')],
    data: {
      title: 'Televisite',
      description: 'Sessioni di telemedicina e token di accesso.',
      endpoints: televisitEndpoints,
      view: 'televisit'
    }
  },
  {
    path: 'doctor/scheduling',
    component: ResourcePageComponent,
    canActivate: [roleGuard('ROLE_DOCTOR')],
    data: {
      title: 'Agenda medico',
      description: 'Gestione slot e appuntamenti dei medici.',
      endpoints: schedulingEndpoints,
      view: 'scheduling'
    }
  },
  {
    path: 'doctor/payments',
    component: ResourcePageComponent,
    canActivate: [roleGuard('ROLE_DOCTOR')],
    data: {
      title: 'Pagamenti',
      description: 'Storico pagamenti e registrazione incassi.',
      endpoints: paymentsEndpoints,
      view: 'payments'
    }
  },
  {
    path: 'doctor/admissions',
    component: ResourcePageComponent,
    canActivate: [roleGuard('ROLE_DOCTOR')],
    data: {
      title: 'Ricoveri',
      description: 'Storico ricoveri del reparto e conferme.',
      endpoints: admissionsEndpoints,
      view: 'admissions'
    }
  },
  {
    path: 'doctor/docs',
    component: DoctorDocsComponent,
    canActivate: [roleGuard('ROLE_DOCTOR')]
  }
];
