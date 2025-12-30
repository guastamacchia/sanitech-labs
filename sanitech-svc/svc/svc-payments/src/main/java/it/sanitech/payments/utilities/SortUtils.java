package it.sanitech.payments.utilities;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Set;

/**
 * Utility per costruire ordinamenti Spring Data in modo sicuro (whitelist).
 */
@UtilityClass
public class SortUtils {

    /**
     * Converte parametri {@code sort} (formato: {@code field[,asc|desc]}) in {@link Sort.Order[]},
     * applicando una whitelist per evitare ordinamenti su campi non previsti.
     */
    public static Sort.Order[] toOrders(String[] sortParams, Set<String> allowedFields, String defaultField) {
        if (sortParams == null || sortParams.length == 0) {
            return new Sort.Order[] { Sort.Order.desc(defaultField) };
        }

        return Arrays.stream(sortParams)
                .filter(s -> s != null && !s.isBlank())
                .map(s -> parseOrder(s, allowedFields, defaultField))
                .toArray(Sort.Order[]::new);
    }

    private static Sort.Order parseOrder(String raw, Set<String> allowedFields, String defaultField) {
        String[] p = raw.split(",", 2);

        String requestedField = p[0].trim();
        String field = allowedFields.contains(requestedField) ? requestedField : defaultField;

        Sort.Direction direction = (p.length > 1 && "asc".equalsIgnoreCase(p[1].trim()))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return new Sort.Order(direction, field);
    }
}
