import { Routes } from '@angular/router';
import { roleGuard } from '@core/auth/role.guard';

export const patientRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('../features/patient-home.component').then(m => m.PatientHomeComponent),
    canActivate: [roleGuard('ROLE_PATIENT')]
  },
  {
    path: 'profile',
    loadComponent: () => import('../features/patient-profile.component').then(m => m.PatientProfileComponent),
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'Il mio profilo',
      description: 'Visualizza e modifica i tuoi dati personali.'
    }
  },
  {
    path: 'consents',
    loadComponent: () => import('../features/consent-management.component').then(m => m.ConsentManagementComponent),
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'Gestione consensi',
      description: 'Gestisci i consensi ai medici per l\'accesso ai tuoi dati clinici.'
    }
  },
  {
    path: 'scheduling',
    loadComponent: () => import('../features/appointment-booking.component').then(m => m.AppointmentBookingComponent),
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'Prenotazioni paziente',
      description: 'Prenota visite mediche e gestisci i tuoi appuntamenti.'
    }
  },
  {
    path: 'docs',
    loadComponent: () => import('../features/patient-documents.component').then(m => m.PatientDocumentsComponent),
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'I miei documenti',
      description: 'Cartella clinica, referti e documenti caricati.'
    }
  },
  {
    path: 'prescriptions',
    loadComponent: () => import('../features/patient-prescriptions.component').then(m => m.PatientPrescriptionsComponent),
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'Le mie prescrizioni',
      description: 'Consulta le prescrizioni e le terapie attive.'
    }
  },
  {
    path: 'televisits',
    loadComponent: () => import('../features/patient-televisits.component').then(m => m.PatientTelevisitsComponent),
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'Le mie televisite',
      description: 'Gestisci le visite in videochiamata.'
    }
  },
  {
    path: 'admissions',
    loadComponent: () => import('../features/patient-admissions.component').then(m => m.PatientAdmissionsComponent),
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'I miei ricoveri',
      description: 'Monitora i ricoveri in corso e consulta lo storico.'
    }
  },
  {
    path: 'notifications',
    loadComponent: () => import('../features/patient-notifications.component').then(m => m.PatientNotificationsComponent),
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'Le mie notifiche',
      description: 'Gestisci le notifiche e i promemoria.'
    }
  },
  {
    path: 'payments',
    loadComponent: () => import('../features/patient-payments.component').then(m => m.PatientPaymentsComponent),
    canActivate: [roleGuard('ROLE_PATIENT')],
    data: {
      title: 'I miei pagamenti',
      description: 'Gestisci i pagamenti e scarica le ricevute.'
    }
  }
];
