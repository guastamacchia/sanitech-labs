package it.sanitech.scheduling.utilities;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Utility minime per esportazioni CSV.
 *
 * <p>
 * Non introduce dipendenze esterne: gestisce escape di base e join di collezioni.
 * </p>
 */
@UtilityClass
public class CsvUtils {

    /**
     * Escape CSV minimale: racchiude tra virgolette e raddoppia eventuali virgolette interne.
     */
    public static String csv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    /**
     * Join di una collezione in una singola stringa (utile per reparti/specializzazioni).
     */
    public static String join(Collection<String> values, String delimiter) {
        if (values == null || values.isEmpty()) return "";
        return values.stream().sorted().collect(Collectors.joining(delimiter));
    }
}
