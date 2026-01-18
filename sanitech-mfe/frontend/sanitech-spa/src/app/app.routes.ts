import { Routes } from '@angular/router';
import { PublicPageComponent } from './features/public/public-page.component';
import { PortalComponent } from './features/portal/portal.component';
import { PortalOverviewComponent } from './features/portal/portal-overview.component';
import { PatientHomeComponent } from './features/portal/patient-home.component';
import { DoctorHomeComponent } from './features/portal/doctor-home.component';
import { AdminHomeComponent } from './features/portal/admin-home.component';
import { ResourcePageComponent } from './features/resources/resource-page.component';
import { authGuard } from './core/auth/auth.guard';
import { roleGuard } from './core/auth/role.guard';

const schedulingEndpoints = [
  {
    label: 'Lista slot disponibili',
    method: 'GET',
    path: '/api/slots'
  },
  {
    label: 'Crea slot',
    method: 'POST',
    path: '/api/slots',
    payload: '{"doctorId": 1, "date": "2024-05-01", "time": "09:30"}'
  },
  {
    label: 'Lista appuntamenti',
    method: 'GET',
    path: '/api/appointments'
  },
  {
    label: 'Prenota appuntamento',
    method: 'POST',
    path: '/api/appointments',
    payload: '{"patientId": 1, "doctorId": 2, "slotId": 10, "reason": "Visita cardiologica"}'
  },
  {
    label: 'Scheduling avanzato',
    method: 'GET',
    path: '/api/scheduling'
  }
];

const docsEndpoints = [
  {
    label: 'Elenco documenti clinici',
    method: 'GET',
    path: '/api/docs'
  },
  {
    label: 'Carica documento',
    method: 'POST',
    path: '/api/docs',
    payload: '{"patientId": 1, "type": "REFERT", "name": "Referto cardiologia"}'
  },
  {
    label: 'Consensi paziente',
    method: 'GET',
    path: '/api/consents'
  },
  {
    label: 'Registra consenso',
    method: 'POST',
    path: '/api/consents',
    payload: '{"patientId": 1, "consentType": "GDPR", "accepted": true}'
  }
];

const notificationsEndpoints = [
  {
    label: 'Lista notifiche',
    method: 'GET',
    path: '/api/notifications'
  },
  {
    label: 'Invia notifica',
    method: 'POST',
    path: '/api/notifications',
    payload: '{"recipient": "anna.conti@sanitech.example", "channel": "EMAIL", "message": "Promemoria visita"}'
  }
];

const paymentsEndpoints = [
  {
    label: 'Lista pagamenti',
    method: 'GET',
    path: '/api/payments'
  },
  {
    label: 'Crea pagamento',
    method: 'POST',
    path: '/api/payments',
    payload: '{"patientId": 1, "amount": 120.0, "currency": "EUR"}'
  },
  {
    label: 'Ricoveri',
    method: 'GET',
    path: '/api/admissions'
  },
  {
    label: 'Registra ricovero',
    method: 'POST',
    path: '/api/admissions',
    payload: '{"patientId": 1, "department": "CARD", "bedId": 4}'
  },
  {
    label: 'Posti letto',
    method: 'GET',
    path: '/api/beds'
  }
];

const prescribingEndpoints = [
  {
    label: 'Lista prescrizioni',
    method: 'GET',
    path: '/api/prescriptions'
  },
  {
    label: 'Crea prescrizione',
    method: 'POST',
    path: '/api/prescribing/prescriptions',
    payload: '{"patientId": 1, "drug": "Atorvastatina", "dosage": "10mg"}'
  }
];

const televisitEndpoints = [
  {
    label: 'Lista sessioni',
    method: 'GET',
    path: '/api/televisit'
  },
  {
    label: 'Avvia sessione',
    method: 'POST',
    path: '/api/televisit',
    payload: '{"appointmentId": 22, "provider": "LIVEKIT"}'
  }
];

const directoryEndpoints = [
  {
    label: 'Lista medici',
    method: 'GET',
    path: '/api/doctors'
  },
  {
    label: 'Crea medico',
    method: 'POST',
    path: '/api/doctors',
    payload: '{"firstName": "Mario", "lastName": "Rossi", "speciality": "CARD"}'
  },
  {
    label: 'Lista pazienti',
    method: 'GET',
    path: '/api/patients'
  },
  {
    label: 'Crea paziente',
    method: 'POST',
    path: '/api/patients',
    payload: '{"firstName": "Anna", "lastName": "Conti", "email": "anna.conti@sanitech.example"}'
  },
  {
    label: 'Reparti',
    method: 'GET',
    path: '/api/departments'
  },
  {
    label: 'Specialità',
    method: 'GET',
    path: '/api/specialities'
  },
  {
    label: 'Admin doctors',
    method: 'GET',
    path: '/api/admin/doctors'
  },
  {
    label: 'Admin patients',
    method: 'GET',
    path: '/api/admin/patients'
  }
];

const auditEndpoints = [
  {
    label: 'Audit trail',
    method: 'GET',
    path: '/api/audit'
  }
];

export const routes: Routes = [
  {
    path: '',
    component: PublicPageComponent
  },
  {
    path: 'portal',
    component: PortalComponent,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        component: PortalOverviewComponent
      },
      {
        path: 'patient',
        component: PatientHomeComponent,
        canActivate: [roleGuard('ROLE_PATIENT')]
      },
      {
        path: 'patient/scheduling',
        component: ResourcePageComponent,
        canActivate: [roleGuard('ROLE_PATIENT')],
        data: {
          title: 'Scheduling paziente',
          description: 'Prenotazioni, slot e appuntamenti disponibili per i cittadini.',
          endpoints: schedulingEndpoints,
          view: 'scheduling'
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
          title: 'Pagamenti e ricoveri',
          description: 'Pagamenti digitali e gestione ricoveri.',
          endpoints: paymentsEndpoints,
          view: 'payments'
        }
      },
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
        path: 'doctor/docs',
        component: ResourcePageComponent,
        canActivate: [roleGuard('ROLE_DOCTOR')],
        data: {
          title: 'Documenti clinici',
          description: 'Documentazione clinica e consensi dei pazienti.',
          endpoints: docsEndpoints,
          view: 'docs'
        }
      },
      {
        path: 'admin',
        component: AdminHomeComponent,
        canActivate: [roleGuard('ROLE_ADMIN')]
      },
      {
        path: 'admin/directory',
        component: ResourcePageComponent,
        canActivate: [roleGuard('ROLE_ADMIN')],
        data: {
          title: 'Directory & anagrafiche',
          description: 'Gestione medici, pazienti, reparti e specialità.',
          endpoints: directoryEndpoints,
          view: 'admin-directory'
        }
      },
      {
        path: 'admin/audit',
        component: ResourcePageComponent,
        canActivate: [roleGuard('ROLE_ADMIN')],
        data: {
          title: 'Audit & compliance',
          description: 'Eventi di audit e tracciamento accessi.',
          endpoints: auditEndpoints,
          view: 'admin-audit'
        }
      },
      {
        path: 'admin/notifications',
        component: ResourcePageComponent,
        canActivate: [roleGuard('ROLE_ADMIN')],
        data: {
          title: 'Notifiche',
          description: 'Monitoraggio e invio notifiche per tutta la piattaforma.',
          endpoints: notificationsEndpoints,
          view: 'notifications'
        }
      },
      {
        path: 'admin/televisit',
        component: ResourcePageComponent,
        canActivate: [roleGuard('ROLE_ADMIN')],
        data: {
          title: 'Televisite & ricoveri',
          description: 'Supervisione sessioni di telemedicina e admissions.',
          endpoints: [...televisitEndpoints, ...paymentsEndpoints],
          view: 'admin-televisit'
        }
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
