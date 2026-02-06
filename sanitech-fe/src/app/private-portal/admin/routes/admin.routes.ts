import { Routes } from '@angular/router';
import { roleGuard } from '@core/auth/role.guard';
import {
  admissionsEndpoints,
  auditEndpoints,
  inPersonVisitsEndpoints,
  notificationsEndpoints,
  televisitEndpoints
} from '../../shared/resources/resource-endpoints';

export const adminRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('../features/admin-home.component').then(m => m.AdminHomeComponent),
    canActivate: [roleGuard('ROLE_ADMIN')]
  },
  {
    path: 'directory',
    loadComponent: () => import('../features/directory/directory-page.component').then(m => m.DirectoryPageComponent),
    canActivate: [roleGuard('ROLE_ADMIN')]
  },
  {
    path: 'audit',
    loadComponent: () => import('../../shared/resources/resource-page.component').then(m => m.ResourcePageComponent),
    canActivate: [roleGuard('ROLE_ADMIN')],
    data: {
      title: 'Audit & compliance',
      description: 'Eventi di audit e tracciamento accessi.',
      endpoints: auditEndpoints,
      view: 'admin-audit'
    }
  },
  {
    path: 'notifications',
    loadComponent: () => import('../../shared/resources/resource-page.component').then(m => m.ResourcePageComponent),
    canActivate: [roleGuard('ROLE_ADMIN')],
    data: {
      title: 'Notifiche',
      description: 'Monitoraggio e invio notifiche per tutta la piattaforma.',
      endpoints: notificationsEndpoints,
      view: 'admin-notifications'
    }
  },
  {
    path: 'payments',
    loadComponent: () => import('../../shared/resources/resource-page.component').then(m => m.ResourcePageComponent),
    canActivate: [roleGuard('ROLE_ADMIN')],
    data: {
      title: 'Pagamenti',
      description: 'Gestione pagamenti, solleciti e statistiche prestazioni.',
      endpoints: notificationsEndpoints,
      view: 'admin-payments'
    }
  },
  {
    path: 'televisit',
    loadComponent: () => import('../../shared/resources/resource-page.component').then(m => m.ResourcePageComponent),
    canActivate: [roleGuard('ROLE_ADMIN')],
    data: {
      title: 'Televisite',
      description: 'Gestione sessioni di telemedicina e conversione appuntamenti.',
      endpoints: televisitEndpoints,
      view: 'admin-televisit'
    }
  },
  {
    path: 'admissions',
    loadComponent: () => import('../../shared/resources/resource-page.component').then(m => m.ResourcePageComponent),
    canActivate: [roleGuard('ROLE_ADMIN')],
    data: {
      title: 'Ricoveri',
      description: 'Gestione ricoveri, posti letto e medici referenti.',
      endpoints: admissionsEndpoints,
      view: 'admin-admissions'
    }
  },
  {
    path: 'in-person-visits',
    loadComponent: () => import('../../shared/resources/resource-page.component').then(m => m.ResourcePageComponent),
    canActivate: [roleGuard('ROLE_ADMIN')],
    data: {
      title: 'Visite in presenza',
      description: 'Gestione appuntamenti in presenza e completamento prestazioni.',
      endpoints: inPersonVisitsEndpoints,
      view: 'admin-in-person-visits'
    }
  }
];
