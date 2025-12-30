package it.sanitech.docs.utilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility per costruire oggetti {@link Sort} in modo sicuro (whitelist dei campi).
 *
 * <p>
 * Scopo: evitare che il client possa richiedere ordinamenti su campi non previsti
 * (o peggio, su proprietà non mappate) generando errori o comportamenti inattesi.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SortUtils {

    /**
     * Converte parametri di sort stile {@code ["field,desc", "other,asc"]} in un {@link Sort}
     * applicando una whitelist dei campi ammessi.
     *
     * @param sortParams parametri raw provenienti da querystring (può essere {@code null})
     * @param allowedFields whitelist campi ordinabili
     * @param defaultField campo di fallback se non viene specificato nulla o se i campi richiesti non sono ammessi
     * @return oggetto {@link Sort} valido
     */
    public static Sort toSafeSort(String[] sortParams, Collection<String> allowedFields, String defaultField) {

        Set<String> allowed = new HashSet<>(allowedFields == null ? List.of() : allowedFields);
        String fallback = (defaultField == null || defaultField.isBlank()) ? "id" : defaultField;

        if (sortParams == null || sortParams.length == 0) {
            return Sort.by(Sort.Direction.ASC, fallback);
        }

        List<Sort.Order> orders = Arrays.stream(sortParams)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(SortUtils::parseOrder)
                .map(o -> allowed.contains(o.getProperty()) ? o : new Sort.Order(o.getDirection(), fallback))
                .collect(Collectors.toList());

        if (orders.isEmpty()) {
            return Sort.by(Sort.Direction.ASC, fallback);
        }
        return Sort.by(orders);
    }

    private static Sort.Order parseOrder(String raw) {
        String[] parts = raw.split(",", 2);
        String field = parts[0].trim();

        Sort.Direction dir = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return new Sort.Order(dir, field);
    }
}
