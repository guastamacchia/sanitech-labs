export const schedulingEndpoints = [
  {
    label: 'Lista slot disponibili',
    method: 'GET',
    path: '/api/slots'
  },
  {
    label: 'Crea slot',
    method: 'POST',
    path: '/api/admin/slots'
  },
  {
    label: 'Lista appuntamenti',
    method: 'GET',
    path: '/api/appointments'
  },
  {
    label: 'Prenota appuntamento',
    method: 'POST',
    path: '/api/appointments'
  }
];

export const docsEndpoints = [
  {
    label: 'Elenco documenti clinici',
    method: 'GET',
    path: '/api/docs'
  },
  {
    label: 'Carica documento',
    method: 'POST',
    path: '/api/docs/upload'
  },
  {
    label: 'Consensi paziente',
    method: 'GET',
    path: '/api/consents/me'
  },
  {
    label: 'Registra consenso',
    method: 'POST',
    path: '/api/consents/me'
  }
];

export const notificationsEndpoints = [
  {
    label: 'Lista notifiche',
    method: 'GET',
    path: '/api/notifications'
  },
  {
    label: 'Invia notifica',
    method: 'POST',
    path: '/api/admin/notifications'
  }
];

export const paymentsEndpoints = [
  {
    label: 'Lista pagamenti',
    method: 'GET',
    path: '/api/payments'
  },
  {
    label: 'Crea pagamento',
    method: 'POST',
    path: '/api/payments'
  }
];

export const admissionsEndpoints = [
  {
    label: 'Lista ricoveri',
    method: 'GET',
    path: '/api/admissions'
  },
  {
    label: 'Registra ricovero',
    method: 'POST',
    path: '/api/admissions'
  },
  {
    label: 'Capacità reparti',
    method: 'GET',
    path: '/api/departments/capacity'
  }
];

export const prescribingEndpoints = [
  {
    label: 'Lista prescrizioni',
    method: 'GET',
    path: '/api/prescriptions'
  },
  {
    label: 'Crea prescrizione',
    method: 'POST',
    path: '/api/doctor/prescriptions'
  }
];

export const televisitEndpoints = [
  {
    label: 'Lista sessioni',
    method: 'GET',
    path: '/api/televisits'
  },
  {
    label: 'Avvia sessione',
    method: 'POST',
    path: '/api/admin/televisits'
  }
];

export const directoryEndpoints = [
  {
    label: 'Lista medici',
    method: 'GET',
    path: '/api/doctors'
  },
  {
    label: 'Admin: crea medico',
    method: 'POST',
    path: '/api/admin/doctors'
  },
  {
    label: 'Lista pazienti',
    method: 'GET',
    path: '/api/patients'
  },
  {
    label: 'Admin: crea paziente',
    method: 'POST',
    path: '/api/admin/patients'
  },
  {
    label: 'Strutture',
    method: 'GET',
    path: '/api/facilities'
  },
  {
    label: 'Reparti',
    method: 'GET',
    path: '/api/departments'
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
  },
  {
    label: 'Admin facilities',
    method: 'GET',
    path: '/api/admin/facilities'
  },
  {
    label: 'Admin: crea struttura',
    method: 'POST',
    path: '/api/admin/facilities'
  },
  {
    label: 'Admin departments',
    method: 'GET',
    path: '/api/admin/departments'
  },
  {
    label: 'Admin: crea reparto',
    method: 'POST',
    path: '/api/admin/departments'
  }
];

export const inPersonVisitsEndpoints = [
  {
    label: 'Lista appuntamenti',
    method: 'GET',
    path: '/api/appointments'
  },
  {
    label: 'Prenota appuntamento',
    method: 'POST',
    path: '/api/appointments'
  },
  {
    label: 'Completa appuntamento',
    method: 'POST',
    path: '/api/appointments/{id}/complete'
  },
  {
    label: 'Ripianifica appuntamento',
    method: 'PATCH',
    path: '/api/appointments/{id}/reschedule'
  },
  {
    label: 'Cambio medico appuntamento',
    method: 'PATCH',
    path: '/api/appointments/{id}/reassign'
  }
];

export const auditEndpoints = [
  {
    label: 'Audit trail',
    method: 'GET',
    path: '/api/audit/events'
  }
];
