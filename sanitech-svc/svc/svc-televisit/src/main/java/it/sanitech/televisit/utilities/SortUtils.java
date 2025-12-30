package it.sanitech.televisit.utilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Set;

/**
 * Utility per creare un {@link Sort} sicuro a partire da parametri di request.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SortUtils {

    /**
     * Converte parametri del tipo {@code sort=field,asc} in un {@link Sort} applicando una whitelist.
     *
     * <p>Se un campo non è consentito, viene sostituito dal {@code defaultField}.</p>
     */
    public static Sort safeSort(String[] sort, Set<String> allowedFields, String defaultField) {
        if (sort == null || sort.length == 0) {
            return Sort.by(defaultField).ascending();
        }

        Sort.Order[] orders = Arrays.stream(sort)
                .map(SortUtils::parseOrder)
                .map(o -> allowedFields.contains(o.getProperty()) ? o : new Sort.Order(o.getDirection(), defaultField))
                .toArray(Sort.Order[]::new);

        return Sort.by(orders);
    }

    private static Sort.Order parseOrder(String raw) {
        if (raw == null || raw.isBlank()) {
            return new Sort.Order(Sort.Direction.ASC, AppConstants.Sort.DEFAULT_FIELD);
        }
        String[] parts = raw.split(",");
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return new Sort.Order(dir, field);
    }
}
