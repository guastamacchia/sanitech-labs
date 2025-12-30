package it.sanitech.admissions.utilities;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Utility per costruire ordinamenti Spring Data in modo sicuro (con whitelist campi).
 */
public final class SortUtils {

    private SortUtils() {}

    /**
     * Costruisce un {@link Sort} sicuro a partire dai parametri {@code sort=campo,dir}.
     *
     * <p>
     * Se il campo richiesto non è nella whitelist, viene sostituito con {@code defaultField}.
     * </p>
     */
    public static Sort safeSort(String[] sortParams, Set<String> allowedFields, String defaultField) {
        if (sortParams == null || sortParams.length == 0) {
            return Sort.by(defaultField).ascending();
        }

        List<Sort.Order> orders = new ArrayList<>();

        Arrays.stream(sortParams)
                .filter(p -> p != null && !p.isBlank())
                .forEach(p -> orders.add(parseOrder(p, allowedFields, defaultField)));

        if (orders.isEmpty()) {
            return Sort.by(defaultField).ascending();
        }

        return Sort.by(orders);
    }

    private static Sort.Order parseOrder(String raw, Set<String> allowedFields, String defaultField) {
        String[] parts = raw.split(",", 2);

        String requestedField = parts[0].trim();
        String field = allowedFields.contains(requestedField) ? requestedField : defaultField;

        Sort.Direction direction = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return new Sort.Order(direction, field);
    }
}
