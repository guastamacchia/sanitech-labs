import { Routes } from '@angular/router';
import { roleGuard } from '../../../core/auth/role.guard';
import { PatientHomeComponent } from './patient-home.component';
import { ConsentManagementComponent } from './consent-management.component';
import { AppointmentBookingComponent } from './appointment-booking.component';
import { ResourcePageComponent } from '../shared/resources/resource-page.component';
import {
  admissionsEndpoints,
  docsEndpoints,
  notificationsEndpoints,
  paymentsEndpoints,
  prescribingEndpoints
} from '../shared/resources/resource-endpoints';

export const patientRoutes: Routes = [
  {
    path: 'patient',
    component: PatientHomeComponent,
    canActivate: [roleGuard('ROLE_PATIENT')]
  },
  {
    path: 'patient/consents',
    component: ConsentManagementComponent,
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'Gestione consensi',
      description: 'Gestisci i consensi ai medici per l\'accesso ai tuoi dati clinici.'
    }
  },
  {
    path: 'patient/scheduling',
    component: AppointmentBookingComponent,
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'Prenotazioni paziente',
      description: 'Prenota visite mediche e gestisci i tuoi appuntamenti.'
    }
  },
  {
    path: 'patient/docs',
    component: ResourcePageComponent,
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'Documenti & consensi',
      description: 'Cartella clinica, referti e consensi informati.',
      endpoints: docsEndpoints,
      view: 'docs'
    }
  },
  {
    path: 'patient/notifications',
    component: ResourcePageComponent,
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'Notifiche paziente',
      description: 'Gestione notifiche multicanale e reminder.',
      endpoints: notificationsEndpoints,
      view: 'notifications'
    }
  },
  {
    path: 'patient/payments',
    component: ResourcePageComponent,
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'Pagamenti',
      description: 'Storico pagamenti effettuati e da effettuare.',
      endpoints: paymentsEndpoints,
      view: 'payments'
    }
  },
  {
    path: 'patient/admissions',
    component: ResourcePageComponent,
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'Ricoveri',
      description: 'Storico ricoveri e conferme ricovero.',
      endpoints: admissionsEndpoints,
      view: 'admissions'
    }
  },
  {
    path: 'patient/prescriptions',
    component: ResourcePageComponent,
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'Prescrizioni paziente',
      description: 'Consulta le prescrizioni e le terapie attive.',
      endpoints: prescribingEndpoints,
      view: 'prescribing'
    }
  }
];
