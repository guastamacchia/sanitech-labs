import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';

/** Definizione di una card della dashboard paziente */
interface DashboardCard {
  id: string;
  title: string;
  description: string;
  icon: string;
  iconColor: string;
  bgColor: string;
  route: string;
  buttonLabel: string;
}

const STORAGE_KEY = 'dashboard-order-patient';

@Component({
  selector: 'app-patient-home',
  standalone: true,
  imports: [CommonModule, RouterLink, DragDropModule],
  templateUrl: './patient-home.component.html',
  styles: [`
    .dashboard-grid {
      display: flex;
      flex-wrap: wrap;
      gap: 1.5rem;
    }
    .dashboard-card-wrapper {
      width: calc(33.333% - 1rem);
    }
    @media (max-width: 991.98px) {
      .dashboard-card-wrapper { width: calc(50% - 0.75rem); }
    }
    @media (max-width: 575.98px) {
      .dashboard-card-wrapper { width: 100%; }
    }
    .dashboard-card-wrapper.cdk-drag-animating {
      transition: transform 250ms cubic-bezier(0, 0, 0.2, 1);
    }
    .cdk-drag-placeholder {
      opacity: 0.25;
      border-radius: 0.75rem;
    }
    .cdk-drop-list-dragging .dashboard-card-wrapper:not(.cdk-drag-placeholder) {
      transition: transform 250ms cubic-bezier(0, 0, 0.2, 1);
    }
    .drag-handle {
      cursor: grab;
      opacity: 0.3;
      transition: opacity 0.2s;
    }
    .drag-handle:active { cursor: grabbing; }
    .card:hover .drag-handle { opacity: 0.6; }
    .lock-toggle {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;
      font-size: 1rem;
    }
  `]
})
export class PatientHomeComponent implements OnInit {
  /** Card della dashboard in ordine corrente */
  cards: DashboardCard[] = [];

  /** Indica se il drag & drop e' attivo */
  isDragEnabled = false;

  /** Definizione iniziale (ordine di default) */
  private readonly defaultCards: DashboardCard[] = [
    {
      id: 'profile',
      title: 'Il mio profilo',
      description: 'Visualizza e modifica i tuoi dati personali, recapiti e preferenze di notifica.',
      icon: 'bi-person-circle',
      iconColor: 'text-primary',
      bgColor: 'bg-primary bg-opacity-10',
      route: '/portal/patient/profile',
      buttonLabel: 'Apri profilo'
    },
    {
      id: 'scheduling',
      title: 'Prenotazioni',
      description: 'Prenota visite mediche, visualizza gli slot disponibili e gestisci i tuoi appuntamenti.',
      icon: 'bi-calendar-check',
      iconColor: 'text-success',
      bgColor: 'bg-success bg-opacity-10',
      route: '/portal/patient/scheduling',
      buttonLabel: 'Gestisci appuntamenti'
    },
    {
      id: 'docs',
      title: 'I miei documenti',
      description: 'Cartella clinica, referti medici e documenti caricati. Carica referti esterni per condividerli con il medico.',
      icon: 'bi-folder2-open',
      iconColor: 'text-info',
      bgColor: 'bg-info bg-opacity-10',
      route: '/portal/patient/docs',
      buttonLabel: 'Vedi documenti'
    },
    {
      id: 'prescriptions',
      title: 'Le mie prescrizioni',
      description: 'Consulta le terapie attive, dosaggi e istruzioni. Visualizza lo storico delle prescrizioni.',
      icon: 'bi-capsule',
      iconColor: 'text-warning',
      bgColor: 'bg-warning bg-opacity-10',
      route: '/portal/patient/prescriptions',
      buttonLabel: 'Vedi prescrizioni'
    },
    {
      id: 'televisits',
      title: 'Le mie televisite',
      description: 'Gestisci le visite in videochiamata, testa i dispositivi e accedi alle sale d\'attesa virtuali.',
      icon: 'bi-camera-video',
      iconColor: 'text-danger',
      bgColor: 'bg-danger bg-opacity-10',
      route: '/portal/patient/televisits',
      buttonLabel: 'Gestisci televisite'
    },
    {
      id: 'admissions',
      title: 'I miei ricoveri',
      description: 'Monitora i ricoveri in corso, timeline degli eventi e storico delle degenze passate.',
      icon: 'bi-hospital',
      iconColor: 'text-secondary',
      bgColor: 'bg-secondary bg-opacity-10',
      route: '/portal/patient/admissions',
      buttonLabel: 'Vedi ricoveri'
    },
    {
      id: 'consents',
      title: 'I miei consensi',
      description: 'Gestisci i consensi ai medici per l\'accesso ai tuoi dati clinici. Controllo totale sulla tua privacy.',
      icon: 'bi-shield-check',
      iconColor: 'text-primary',
      bgColor: 'bg-primary bg-opacity-10',
      route: '/portal/patient/consents',
      buttonLabel: 'Gestisci consensi'
    },
    {
      id: 'notifications',
      title: 'Le mie notifiche',
      description: 'Promemoria appuntamenti, avvisi su nuovi documenti e notifiche sugli accessi ai tuoi dati.',
      icon: 'bi-bell',
      iconColor: 'text-info',
      bgColor: 'bg-info bg-opacity-10',
      route: '/portal/patient/notifications',
      buttonLabel: 'Vedi notifiche'
    },
    {
      id: 'payments',
      title: 'I miei pagamenti',
      description: 'Paga ticket sanitari, visualizza lo storico e scarica ricevute per la dichiarazione dei redditi.',
      icon: 'bi-credit-card',
      iconColor: 'text-success',
      bgColor: 'bg-success bg-opacity-10',
      route: '/portal/patient/payments',
      buttonLabel: 'Gestisci pagamenti'
    }
  ];

  ngOnInit(): void {
    this.cards = this.loadOrder();
  }

  /** Gestisce il drop di una card nella nuova posizione */
  onDrop(event: CdkDragDrop<DashboardCard[]>): void {
    moveItemInArray(this.cards, event.previousIndex, event.currentIndex);
    this.saveOrder();
  }

  /** Ripristina l'ordine di default e rimuove il salvataggio */
  resetOrder(): void {
    this.cards = [...this.defaultCards];
    try { localStorage.removeItem(STORAGE_KEY); } catch { /* noop */ }
  }

  /** Carica l'ordine salvato da localStorage, o ritorna l'ordine di default */
  private loadOrder(): DashboardCard[] {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        const ids: string[] = JSON.parse(saved);
        const cardMap = new Map(this.defaultCards.map(c => [c.id, c]));
        const ordered = ids
          .map(id => cardMap.get(id))
          .filter((c): c is DashboardCard => !!c);
        const savedSet = new Set(ids);
        const newCards = this.defaultCards.filter(c => !savedSet.has(c.id));
        return [...ordered, ...newCards];
      }
    } catch { /* localStorage non disponibile o corrotto */ }
    return [...this.defaultCards];
  }

  /** Persiste l'ordine corrente in localStorage */
  private saveOrder(): void {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.cards.map(c => c.id)));
    } catch { /* localStorage non disponibile */ }
  }
}
