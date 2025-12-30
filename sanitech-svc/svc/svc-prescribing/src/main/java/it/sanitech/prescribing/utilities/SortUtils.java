package it.sanitech.prescribing.utilities;

import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Stream;

/**
 * Utility per normalizzare e "sanitizzare" l'ordinamento richiesto dal client.
 *
 * <p>
 * Scopo: evitare che un parametro {@code sort} arbitrario possa causare errori runtime
 * (campo inesistente) o query non ottimali. Vengono accettati solo campi presenti in una whitelist.
 * </p>
 */
public final class SortUtils {

    private SortUtils() {
    }

    /**
     * Costruisce un {@link Sort} a partire dai parametri {@code sort} (stile Spring Data),
     * applicando una whitelist di campi consentiti.
     *
     * <p>Esempi di input:</p>
     * <ul>
     *   <li>{@code sort=id}</li>
     *   <li>{@code sort=createdAt,desc}</li>
     *   <li>{@code sort=patientId,asc&sort=id,desc}</li>
     * </ul>
     *
     * @param sortParams  array di parametri sort, es. ["createdAt,desc","id,asc"]
     * @param allowed     campi ammessi per l'ordinamento
     * @param defaultField campo di fallback se il client richiede un campo non ammesso o non valorizza sort
     * @return Sort validato e coerente
     */
    public static Sort parse(String[] sortParams, Set<String> allowed, String defaultField) {
        if (sortParams == null || sortParams.length == 0) {
            return Sort.by(defaultField).ascending();
        }

        List<Sort.Order> orders = Stream.of(sortParams)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(s -> s.split(",", 2))
                .map(parts -> new Sort.Order(parseDirection(parts), sanitizeField(parts[0], allowed, defaultField)))
                .toList();

        return orders.isEmpty() ? Sort.by(defaultField).ascending() : Sort.by(orders);
    }

    private static Sort.Direction parseDirection(String[] parts) {
        if (parts.length < 2) {
            return Sort.Direction.ASC;
        }
        return "desc".equalsIgnoreCase(parts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    private static String sanitizeField(String requested, Set<String> allowed, String defaultField) {
        String field = (requested == null) ? "" : requested.trim();
        return allowed.contains(field) ? field : defaultField;
    }

    /**
     * Helper per creare una whitelist immutabile evitando costruttori verbosi nei controller.
     *
     * @param fields campi ammessi
     * @return Set non modificabile
     */
    public static Set<String> allowedFields(String... fields) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(fields)));
    }
}
