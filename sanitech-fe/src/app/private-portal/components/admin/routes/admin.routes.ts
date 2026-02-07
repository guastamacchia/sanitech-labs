import { Routes } from '@angular/router';
import { roleGuard } from '@core/auth/role.guard';

export const adminRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('../components/admin-home.component').then(m => m.AdminHomeComponent),
    canActivate: [roleGuard('ROLE_ADMIN')]
  },
  {
    path: 'directory',
    loadComponent: () => import('../components/sections/directory/directory-page.component').then(m => m.DirectoryPageComponent),
    canActivate: [roleGuard('ROLE_ADMIN')]
  },
  {
    path: 'audit',
    loadComponent: () => import('../components/sections/audit/admin-audit.component').then(m => m.AdminAuditComponent),
    canActivate: [roleGuard('ROLE_ADMIN')]
  },
  {
    path: 'notifications',
    loadComponent: () => import('../components/sections/notifications/admin-notifications.component').then(m => m.AdminNotificationsComponent),
    canActivate: [roleGuard('ROLE_ADMIN')]
  },
  {
    path: 'payments',
    loadComponent: () => import('../components/sections/payments/admin-payments.component').then(m => m.AdminPaymentsComponent),
    canActivate: [roleGuard('ROLE_ADMIN')]
  },
  {
    path: 'televisit',
    loadComponent: () => import('../components/sections/televisit/admin-televisit.component').then(m => m.AdminTelevisitComponent),
    canActivate: [roleGuard('ROLE_ADMIN')]
  },
  {
    path: 'admissions',
    loadComponent: () => import('../components/sections/admissions/admin-admissions.component').then(m => m.AdminAdmissionsComponent),
    canActivate: [roleGuard('ROLE_ADMIN')]
  },
  {
    path: 'in-person-visits',
    loadComponent: () => import('../components/sections/in-person-visits/admin-in-person-visits.component').then(m => m.AdminInPersonVisitsComponent),
    canActivate: [roleGuard('ROLE_ADMIN')]
  }
];
