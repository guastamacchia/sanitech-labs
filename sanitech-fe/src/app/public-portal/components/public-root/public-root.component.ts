import { Component } from '@angular/core';
import { HeroSectionComponent } from '../sections/hero/hero-section.component';
import { ServiziSectionComponent } from '../sections/servizi/servizi-section.component';
import { TestimonianzeSectionComponent } from '../sections/testimonianze/testimonianze-section.component';
import { StruttureSectionComponent } from '../sections/strutture/strutture-section.component';
import { ChiSiamoSectionComponent } from '../sections/chi-siamo/chi-siamo-section.component';
import { ContattiSectionComponent } from '../sections/contatti/contatti-section.component';

/**
 * Componente root del portale pubblico.
 * Orchestra le sezioni della landing page e gestisce lo stato del tab di accesso.
 */
@Component({
  selector: 'app-public-root',
  standalone: true,
  imports: [
    HeroSectionComponent,
    ServiziSectionComponent,
    TestimonianzeSectionComponent,
    StruttureSectionComponent,
    ChiSiamoSectionComponent,
    ContattiSectionComponent
  ],
  templateUrl: './public-root.component.html'
})
export class PublicRootComponent {
  /** Tab attivo nella card di accesso/registrazione */
  activeAccessTab: 'login' | 'register' = 'login';

  /** Callback per cambio tab (ricevuto dalla sezione hero) */
  onTabChange(tab: 'login' | 'register'): void {
    this.activeAccessTab = tab;
  }
}
