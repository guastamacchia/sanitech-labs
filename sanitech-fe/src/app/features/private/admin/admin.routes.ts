import { Routes } from '@angular/router';
import { roleGuard } from '../../../core/auth/role.guard';
import { AdminHomeComponent } from './admin-home.component';
import { ResourcePageComponent } from '../shared/resources/resource-page.component';
import { DirectoryPageComponent } from './directory/directory-page.component';
import {
  admissionsEndpoints,
  auditEndpoints,
  directoryEndpoints,
  inPersonVisitsEndpoints,
  notificationsEndpoints,
  televisitEndpoints
} from '../shared/resources/resource-endpoints';

export const adminRoutes: Routes = [
  {
    path: 'admin',
    component: AdminHomeComponent,
    canActivate: [roleGuard('ROLE_ADMIN')]
  },
  {
    path: 'admin/directory',
    component: DirectoryPageComponent,
    canActivate: [roleGuard('ROLE_ADMIN')]
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
      view: 'admin-notifications'
    }
  },
  {
    path: 'admin/payments',
    component: ResourcePageComponent,
    canActivate: [roleGuard('ROLE_ADMIN')],
    data: {
      title: 'Pagamenti',
      description: 'Gestione pagamenti, solleciti e statistiche prestazioni.',
      endpoints: notificationsEndpoints,
      view: 'admin-payments'
    }
  },
  {
    path: 'admin/televisit',
    component: ResourcePageComponent,
    canActivate: [roleGuard('ROLE_ADMIN')],
    data: {
      title: 'Televisite',
      description: 'Gestione sessioni di telemedicina e conversione appuntamenti.',
      endpoints: televisitEndpoints,
      view: 'admin-televisit'
    }
  },
  {
    path: 'admin/admissions',
    component: ResourcePageComponent,
    canActivate: [roleGuard('ROLE_ADMIN')],
    data: {
      title: 'Ricoveri',
      description: 'Gestione ricoveri, posti letto e medici referenti.',
      endpoints: admissionsEndpoints,
      view: 'admin-admissions'
    }
  },
  {
    path: 'admin/in-person-visits',
    component: ResourcePageComponent,
    canActivate: [roleGuard('ROLE_ADMIN')],
    data: {
      title: 'Visite in presenza',
      description: 'Gestione appuntamenti in presenza e completamento prestazioni.',
      endpoints: inPersonVisitsEndpoints,
      view: 'admin-in-person-visits'
    }
  }
];
