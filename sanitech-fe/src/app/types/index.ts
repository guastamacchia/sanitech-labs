// Barrel export tipi condivisi
// I DTO e le interfacce cross-area vanno qui.
// I tipi specifici per ruolo restano nei rispettivi servizi di area.

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
