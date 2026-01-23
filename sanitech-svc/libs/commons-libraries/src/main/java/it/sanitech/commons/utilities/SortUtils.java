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
 *
 * <p>
 * Policy di sicurezza:
 * <ul>
 *   <li>se un campo non è nella whitelist, viene sostituito con {@code defaultField} e la direzione viene forzata a ASC</li>
 *   <li>se {@code sortParams} è nullo/vuoto o produce ordini vuoti, si ritorna un sort su {@code defaultField ASC}</li>
 * </ul>
 * </p>
 */
@UtilityClass
public class SortUtils {

    /**
     * Costruisce un {@link Sort} validando i campi richiesti rispetto alla whitelist.
     *
     * @param sortParams    parametri raw (es. {@code ["lastName,asc","id,desc"]})
     * @param allowedFields whitelist di campi ordinabili (non-null)
     * @param defaultField  campo fallback se il parametro non è valido o mancante (non-blank)
     * @return Sort valido e deterministico
     */
    public static Sort safeSort(String[] sortParams, Set<String> allowedFields, String defaultField) {
        // 1) Normalizzazione input (fail-safe)
        Set<String> safeAllowed = Objects.nonNull(allowedFields) ? allowedFields : Set.of();
        String safeDefault = (Objects.nonNull(defaultField) && !defaultField.isBlank()) ? defaultField : "id";

        // 2) Stream dei parametri sort (null-safe)
        Stream<String> params = Objects.isNull(sortParams) ? Stream.empty() : Arrays.stream(sortParams);

        // 3) Sanitizzazione e parsing
        List<Sort.Order> orders = params
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(raw -> toOrder(raw, safeAllowed, safeDefault))
                .toList();

        // 4) Fallback deterministico
        return orders.isEmpty() ? Sort.by(safeDefault).ascending() : Sort.by(orders);
    }

    /**
     * Converte un parametro raw (es. {@code "lastName,desc"}) in un {@link Sort.Order}
     * applicando whitelist, normalizzazione e fallback deterministico.
     *
     * <p>
     * Regole:
     * <ul>
     *   <li>raw nullo/vuoto → {@code defaultField ASC}</li>
     *   <li>campo non in whitelist → {@code defaultField ASC}</li>
     *   <li>direzione supportata: {@code asc|desc} (default ASC)</li>
     * </ul>
     * </p>
     */
    private static Sort.Order toOrder(String raw, Set<String> allowedFields, String defaultField) {

        // 1) Fail-safe immediato: input nullo o vuoto
        if (Objects.isNull(raw) || raw.isBlank()) {
            return new Sort.Order(Sort.Direction.ASC, defaultField);
        }

        // 2) Split controllato: "field" | "field,dir"
        String[] parts = raw.split(",", 2);

        // 3) Normalizzazione campo richiesto
        String requestedField = parts[0].trim();

        // 4) Verifica whitelist (policy di sicurezza)
        boolean allowed = !requestedField.isBlank() && allowedFields.contains(requestedField);
        String safeField = allowed ? requestedField : defaultField;

        // 5) Parsing direzione (solo se il campo è valido)
        Sort.Direction direction = Sort.Direction.ASC;
        if (allowed && parts.length == 2 && Objects.nonNull(parts[1])) {
            String dir = parts[1].trim();
            if ("desc".equalsIgnoreCase(dir)) {
                direction = Sort.Direction.DESC;
            }
        }

        // 6) Creazione ordine
        return new Sort.Order(direction, safeField);
    }
}
