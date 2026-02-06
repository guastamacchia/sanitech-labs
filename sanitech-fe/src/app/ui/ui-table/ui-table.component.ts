import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Componente presentazionale per tabelle dati.
 * Racchiude il markup Bootstrap (table-responsive + table) e gestisce
 * le due varianti CSS tramite @Input.
 *
 * Uso:
 * <ui-table [hover]="true">
 *   <thead class="table-light">...</thead>
 *   <tbody>...</tbody>
 * </ui-table>
 */
@Component({
  selector: 'ui-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ui-table.component.html'
})
export class UiTableComponent {
  /** Abilita table-hover e mb-0 (variante feature component) */
  @Input() hover = false;
  /** Aggiunge mt-2 al wrapper table-responsive */
  @Input() marginTop = false;
}
