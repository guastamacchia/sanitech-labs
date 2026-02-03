package it.sanitech.payments.services.dto;

/**
 * DTO per le statistiche delle prestazioni sanitarie.
 */
public record ServicePerformedStatsDto(
        /** Totale prestazioni nel periodo */
        long totalServices,

        /** Prestazioni pagate entro 7 giorni */
        long paidWithin7Days,

        /** Prestazioni pagate dopo sollecito */
        long paidWithReminder,

        /** Prestazioni ancora in sospeso */
        long stillPending,

        /** Percentuale pagate entro 7 giorni */
        double percentWithin7Days,

        /** Percentuale pagate con sollecito */
        double percentWithReminder,

        /** Percentuale ancora in sospeso */
        double percentPending,

        /** Totale prestazioni filtrate (per UI) */
        long filteredCount
) {}
