import { Routes } from '@angular/router';
import { roleGuard } from '../../../core/auth/role.guard';
import { PatientHomeComponent } from './patient-home.component';
import { ConsentManagementComponent } from './consent-management.component';
import { AppointmentBookingComponent } from './appointment-booking.component';
import { PatientProfileComponent } from './patient-profile.component';
import { PatientDocumentsComponent } from './patient-documents.component';
import { PatientPrescriptionsComponent } from './patient-prescriptions.component';
import { PatientTelevisitsComponent } from './patient-televisits.component';
import { PatientAdmissionsComponent } from './patient-admissions.component';
import { PatientNotificationsComponent } from './patient-notifications.component';
import { PatientPaymentsComponent } from './patient-payments.component';

export const patientRoutes: Routes = [
  {
    path: 'patient',
    component: PatientHomeComponent,
    canActivate: [roleGuard('ROLE_PATIENT')]
  },
  {
    path: 'patient/profile',
    component: PatientProfileComponent,
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'Il mio profilo',
      description: 'Visualizza e modifica i tuoi dati personali.'
    }
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
    component: PatientDocumentsComponent,
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'I miei documenti',
      description: 'Cartella clinica, referti e documenti caricati.'
    }
  },
  {
    path: 'patient/prescriptions',
    component: PatientPrescriptionsComponent,
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'Le mie prescrizioni',
      description: 'Consulta le prescrizioni e le terapie attive.'
    }
  },
  {
    path: 'patient/televisits',
    component: PatientTelevisitsComponent,
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'Le mie televisite',
      description: 'Gestisci le visite in videochiamata.'
    }
  },
  {
    path: 'patient/admissions',
    component: PatientAdmissionsComponent,
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'I miei ricoveri',
      description: 'Monitora i ricoveri in corso e consulta lo storico.'
    }
  },
  {
    path: 'patient/notifications',
    component: PatientNotificationsComponent,
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'Le mie notifiche',
      description: 'Gestisci le notifiche e i promemoria.'
    }
  },
  {
    path: 'patient/payments',
    component: PatientPaymentsComponent,
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'I miei pagamenti',
      description: 'Gestisci i pagamenti e scarica le ricevute.'
    }
  }
];
