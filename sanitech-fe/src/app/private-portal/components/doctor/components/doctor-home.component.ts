import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';

/** Definizione di una card della dashboard medico */
interface DashboardCard {
  id: string;
  title: string;
  description: string;
  icon: string;
  iconColor: string;
  bgColor: string;
  bgStyle?: string;
  iconStyle?: string;
  route: string;
  buttonLabel: string;
}

const STORAGE_KEY = 'dashboard-order-doctor';

@Component({
  selector: 'app-doctor-home',
  standalone: true,
  imports: [CommonModule, RouterLink, DragDropModule],
  templateUrl: './doctor-home.component.html',
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
export class DoctorHomeComponent implements OnInit {
  /** Card della dashboard in ordine corrente */
  cards: DashboardCard[] = [];

  /** Indica se il drag & drop e' attivo */
  isDragEnabled = false;

  /** Definizione iniziale (ordine di default) */
  private readonly defaultCards: DashboardCard[] = [
    {
      id: 'profile',
      title: 'Il mio profilo',
      description: 'Dati professionali, specializzazioni e orari di disponibilità.',
      icon: 'bi-person-badge',
      iconColor: 'text-primary',
      bgColor: 'bg-primary bg-opacity-10',
      route: '/portal/doctor/profile',
      buttonLabel: 'Gestisci profilo'
    },
    {
      id: 'patients',
      title: 'I miei pazienti',
      description: 'Pazienti con consenso attivo, ricerca e accesso alle cartelle cliniche.',
      icon: 'bi-people',
      iconColor: 'text-success',
      bgColor: 'bg-success bg-opacity-10',
      route: '/portal/doctor/patients',
      buttonLabel: 'Gestisci pazienti'
    },
    {
      id: 'clinical-docs',
      title: 'Documenti clinici',
      description: 'Carica referti, visualizza esami e gestisci la documentazione clinica.',
      icon: 'bi-file-earmark-medical',
      iconColor: 'text-info',
      bgColor: 'bg-info bg-opacity-10',
      route: '/portal/doctor/clinical-docs',
      buttonLabel: 'Gestisci documenti'
    },
    {
      id: 'prescriptions',
      title: 'Prescrizioni',
      description: 'Emetti prescrizioni farmacologiche, rinnova terapie e visualizza lo storico.',
      icon: 'bi-prescription2',
      iconColor: 'text-warning',
      bgColor: 'bg-warning bg-opacity-10',
      route: '/portal/doctor/prescriptions',
      buttonLabel: 'Gestisci prescrizioni'
    },
    {
      id: 'agenda',
      title: 'La mia agenda',
      description: 'Gestisci slot, disponibilità settimanali e appuntamenti prenotati.',
      icon: 'bi-calendar-week',
      iconColor: 'text-danger',
      bgColor: 'bg-danger bg-opacity-10',
      route: '/portal/doctor/agenda',
      buttonLabel: 'Apri agenda'
    },
    {
      id: 'televisits',
      title: 'Le mie televisite',
      description: 'Prepara e conduci visite a distanza, condividi documenti durante la sessione.',
      icon: 'bi-camera-video',
      iconColor: '',
      bgColor: '',
      bgStyle: 'background-color: rgba(111, 66, 193, 0.1)',
      iconStyle: 'color: #6f42c1',
      route: '/portal/doctor/televisits',
      buttonLabel: 'Gestisci televisite'
    },
    {
      id: 'admissions',
      title: 'I miei ricoveri',
      description: 'Pazienti ricoverati nel tuo reparto, presa in carico e dimissioni.',
      icon: 'bi-hospital',
      iconColor: 'text-secondary',
      bgColor: 'bg-secondary bg-opacity-10',
      route: '/portal/doctor/admissions',
      buttonLabel: 'Gestisci ricoveri'
    },
    {
      id: 'notifications',
      title: 'Notifiche',
      description: 'Consensi, appuntamenti, documenti caricati e avvisi del sistema.',
      icon: 'bi-bell',
      iconColor: 'text-danger',
      bgColor: 'bg-danger bg-opacity-10',
      route: '/portal/doctor/notifications',
      buttonLabel: 'Visualizza notifiche'
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
