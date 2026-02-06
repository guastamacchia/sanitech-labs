// Barrel export utilità - funzioni pure (senza dipendenze da framework)

/**
 * Formatta una stringa data in formato italiano dd/MM/yyyy.
 */
export function formatDateIt(dateStr: string): string {
  if (!dateStr) return '-';
  const d = new Date(dateStr);
  return d.toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

/**
 * Formatta una stringa data in formato italiano dd/MM/yyyy HH:mm.
 */
export function formatDateTimeIt(dateStr: string): string {
  if (!dateStr) return '-';
  const d = new Date(dateStr);
  return d.toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit', year: 'numeric' }) +
    ' ' + d.toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' });
}

/**
 * Calcola il numero totale di pagine dato il conteggio elementi e la dimensione pagina.
 */
export function totalPages(itemCount: number, pageSize: number): number {
  return Math.max(1, Math.ceil(itemCount / pageSize));
}
