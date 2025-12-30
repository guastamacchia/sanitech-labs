package it.sanitech.directory.utilities;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Utility per costruire {@link Pageable} con limiti di sicurezza (max page size).
 */
@UtilityClass
public class PageableUtils {

    /**
     * Crea un {@link Pageable} validando {@code page} e {@code size}.
     *
     * <p>
     * Nota: questo controllo evita richieste con {@code size} eccessivo che potrebbero
     * degradare il DB o il servizio.
     * </p>
     */
    public static Pageable pageRequest(int page, int size, int maxSize, Sort sort) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), maxSize);
        return PageRequest.of(safePage, safeSize, sort);
    }
}
