export const schedulingEndpoints = [
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

export const docsEndpoints = [
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

export const notificationsEndpoints = [
  {
    label: 'Lista notifiche',
    method: 'GET',
    path: '/api/notifications'
  },
  {
    label: 'Invia notifica',
    method: 'POST',
    path: '/api/notifications',
    payload:
      '{"recipient": "anna.conti@sanitech.example", "channel": "EMAIL", "subject": "Promemoria visita", "message": "Promemoria visita", "notes": "Arrivare 10 minuti prima"}'
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
    path: '/api/payments',
    payload: '{"patientId": 1, "amount": 120.0, "currency": "EUR", "service": "Visita medica con Dott. Marco Bianchi"}'
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
    path: '/api/admissions',
    payload: '{"patientId": 1, "department": "CARD", "bedId": 4, "notes": "Monitoraggio ECG"}'
  },
  {
    label: 'Posti letto',
    method: 'GET',
    path: '/api/beds'
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
    path: '/api/prescribing/prescriptions',
    payload: '{"patientId": 1, "drug": "Atorvastatina", "dosage": "10mg"}'
  }
];

export const televisitEndpoints = [
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

export const directoryEndpoints = [
  {
    label: 'Lista medici',
    method: 'GET',
    path: '/api/doctors'
  },
  {
    label: 'Admin: crea medico',
    method: 'POST',
    path: '/api/admin/doctors',
    payload: '{"firstName": "Mario", "lastName": "Rossi", "speciality": "CARD"}'
  },
  {
    label: 'Lista pazienti',
    method: 'GET',
    path: '/api/patients'
  },
  {
    label: 'Admin: crea paziente',
    method: 'POST',
    path: '/api/admin/patients',
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

export const auditEndpoints = [
  {
    label: 'Audit trail',
    method: 'GET',
    path: '/api/audit'
  }
];
