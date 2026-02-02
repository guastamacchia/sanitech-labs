import { Routes } from '@angular/router';
import { roleGuard } from '../../../core/auth/role.guard';
import { AdminHomeComponent } from './admin-home.component';
import { ResourcePageComponent } from '../shared/resources/resource-page.component';
import { DirectoryPageComponent } from './directory/directory-page.component';
import {
  admissionsEndpoints,
  auditEndpoints,
  directoryEndpoints,
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
      view: 'notifications'
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
  }
];
