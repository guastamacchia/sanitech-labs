package it.sanitech.commons.utilities;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

import java.util.*;
import java.util.stream.Stream;

/**
 * Utility per costruire un {@link Sort} sicuro a partire dai parametri query {@code sort=...}.
 *
 * <p>
 * Obiettivi:
 * <ul>
 *   <li>evitare sorting su campi non previsti (whitelist);</li>
 *   <li>normalizzare direzione (asc/desc);</li>
 *   <li>garantire un fallback deterministico.</li>
 * </ul>
 * </p>
 */
@UtilityClass
public class SortUtils {

    /**
     * Costruisce un {@link Sort} validando i campi richiesti rispetto alla whitelist.
     *
     * @param sortParams    parametri raw (es. {@code ["lastName,asc","id,desc"]})
     * @param allowedFields whitelist di campi ordinabili
     * @param defaultField  campo fallback se il parametro non è valido o mancante
     * @return Sort valido e deterministico
     */
    public static Sort safeSort(String[] sortParams, Set<String> allowedFields, String defaultField) {
        List<Sort.Order> orders = Stream.of(Optional.ofNullable(sortParams).orElse(new String[0]))
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(raw -> toOrder(raw, allowedFields, defaultField))
                .toList();

        if (orders.isEmpty()) {
            return Sort.by(defaultField).ascending();
        }
        return Sort.by(orders);
    }

    private static Sort.Order toOrder(String raw, Set<String> allowedFields, String defaultField) {
        String[] parts = raw.split(",", 2);
        String requestedField = parts[0].trim();

        String safeField = allowedFields.contains(requestedField) ? requestedField : defaultField;

        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length == 2 && "desc".equalsIgnoreCase(parts[1].trim())) {
            direction = Sort.Direction.DESC;
        }

        // Se il campo richiesto non è consentito, usiamo sempre la direzione di default (ASC)
        if (!safeField.equals(requestedField)) {
            direction = Sort.Direction.ASC;
        }

        return new Sort.Order(direction, safeField);
    }
}
