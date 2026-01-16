package it.sanitech.notifications.utilities;

import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Utility per costruire un {@link Sort} in modo sicuro partendo da parametri REST.
 *
 * <p>
 * Obiettivo: prevenire “sort injection” o sort su campi non indicizzati/non previsti.
 * Il chiamante passa la whitelist dei campi consentiti; in caso di campo non ammesso
 * si ricade su un default.
 * </p>
 *
 * <p>Esempio di input supportato: {@code sort=id,desc&sort=createdAt,asc}</p>
 */
public final class SortUtils {

    private SortUtils() { }

    /**
     * Converte l'array di stringhe {@code sort} in un oggetto {@link Sort} applicando una whitelist.
     *
     * @param sort          parametri sort del controller (es. {@code ["id,desc", "createdAt,asc"]})
     * @param defaultField  campo di fallback se il campo richiesto non è ammesso
     * @param allowedFields whitelist di campi ordinabili esposti via API
     * @return Sort sicuro (mai null)
     */
    public static Sort safeSort(String[] sort, String defaultField, String... allowedFields) {
        Set<String> allowed = (allowedFields == null || allowedFields.length == 0)
                ? Set.of(defaultField)
                : Set.of(allowedFields);

        if (sort == null || sort.length == 0) {
            return Sort.by(defaultField).ascending();
        }

        List<Sort.Order> orders = Arrays.stream(sort)
                .filter(StringUtils::hasText)
                .map(s -> parseOrder(s, allowed, defaultField))
                .filter(Objects::nonNull)
                .toList();

        return orders.isEmpty() ? Sort.by(defaultField).ascending() : Sort.by(orders);
    }

    private static Sort.Order parseOrder(String raw, Set<String> allowed, String defaultField) {
        String[] parts = raw.split(",", 2);
        String requestedField = parts[0].trim();

        String field = allowed.contains(requestedField) ? requestedField : defaultField;

        Sort.Direction direction = (parts.length == 2 && "desc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return new Sort.Order(direction, field);
    }
}
