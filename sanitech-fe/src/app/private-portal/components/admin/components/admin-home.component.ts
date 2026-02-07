import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';

/** Definizione di una card della dashboard admin */
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

const STORAGE_KEY = 'dashboard-order-admin';

@Component({
  selector: 'app-admin-home',
  standalone: true,
  imports: [CommonModule, RouterLink, DragDropModule],
  templateUrl: './admin-home.component.html',
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
export class AdminHomeComponent implements OnInit {
  /** Card della dashboard in ordine corrente */
  cards: DashboardCard[] = [];

  /** Indica se il drag & drop e' attivo */
  isDragEnabled = false;

  /** Definizione iniziale (ordine di default) */
  private readonly defaultCards: DashboardCard[] = [
    {
      id: 'directory',
      title: 'Anagrafiche',
      description: 'Gestione anagrafiche di medici, pazienti, strutture sanitarie e reparti. Attivazione account e trasferimenti.',
      icon: 'bi-people',
      iconColor: 'text-primary',
      bgColor: 'bg-primary bg-opacity-10',
      route: '/portal/admin/directory',
      buttonLabel: 'Gestisci anagrafiche'
    },
    {
      id: 'audit',
      title: 'Audit & Compliance',
      description: 'Traccia eventi di sistema, accessi ai dati clinici e verifiche GDPR. Report di conformita\' e audit trail.',
      icon: 'bi-shield-check',
      iconColor: 'text-info',
      bgColor: 'bg-info bg-opacity-10',
      route: '/portal/admin/audit',
      buttonLabel: 'Visualizza audit'
    },
    {
      id: 'notifications',
      title: 'Notifiche',
      description: 'Monitoraggio e invio notifiche email e SMS per tutta la piattaforma.',
      icon: 'bi-bell',
      iconColor: 'text-warning',
      bgColor: 'bg-warning bg-opacity-10',
      route: '/portal/admin/notifications',
      buttonLabel: 'Gestisci notifiche'
    },
    {
      id: 'payments',
      title: 'Pagamenti',
      description: 'Gestione pagamenti, solleciti e statistiche prestazioni sanitarie.',
      icon: 'bi-credit-card',
      iconColor: '',
      bgColor: '',
      bgStyle: 'background: rgba(111, 66, 193, 0.1)',
      iconStyle: 'color: #6f42c1',
      route: '/portal/admin/payments',
      buttonLabel: 'Gestisci pagamenti'
    },
    {
      id: 'televisit',
      title: 'Televisite',
      description: 'Gestione sessioni di telemedicina, monitoraggio conversioni e statistiche di utilizzo del servizio.',
      icon: 'bi-camera-video',
      iconColor: 'text-danger',
      bgColor: 'bg-danger bg-opacity-10',
      route: '/portal/admin/televisit',
      buttonLabel: 'Gestisci televisite'
    },
    {
      id: 'admissions',
      title: 'Ricoveri',
      description: 'Gestione posti letto, assegnazione medici referenti e monitoraggio capacita\' reparti.',
      icon: 'bi-hospital',
      iconColor: 'text-success',
      bgColor: 'bg-success bg-opacity-10',
      route: '/portal/admin/admissions',
      buttonLabel: 'Gestisci ricoveri'
    },
    {
      id: 'in-person-visits',
      title: 'Visite in presenza',
      description: 'Gestione visite in presenza, completamento prestazioni e monitoraggio appuntamenti programmati.',
      icon: 'bi-calendar-check',
      iconColor: '',
      bgColor: '',
      bgStyle: 'background: rgba(13, 148, 136, 0.1)',
      iconStyle: 'color: #0d9488',
      route: '/portal/admin/in-person-visits',
      buttonLabel: 'Gestisci visite'
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
        /* Aggiunge eventuali nuove card non presenti nel salvataggio */
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
