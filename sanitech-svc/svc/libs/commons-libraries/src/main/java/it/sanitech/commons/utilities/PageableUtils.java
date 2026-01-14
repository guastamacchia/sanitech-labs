package it.sanitech.commons.utilities;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Objects;

/**
 * Utility per costruire {@link Pageable} applicando limiti di sicurezza
 * su paginazione e dimensione pagina.
 *
 * <p>
 * Obiettivi:
 * <ul>
 *   <li>evitare page negative</li>
 *   <li>evitare size troppo piccole o eccessive</li>
 *   <li>proteggere database e servizio da richieste abusive</li>
 * </ul>
 * </p>
 */
@UtilityClass
public class PageableUtils {

    private static final int MIN_PAGE = 0;
    private static final int MIN_SIZE = 1;

    /**
     * Crea un {@link Pageable} validando {@code page} e {@code size}.
     *
     * <p>
     * Regole applicate:
     * <ul>
     *   <li>{@code page < 0} → {@code 0}</li>
     *   <li>{@code size < 1} → {@code 1}</li>
     *   <li>{@code size > maxSize} → {@code maxSize}</li>
     * </ul>
     * </p>
     *
     * @param page     indice pagina richiesto (0-based)
     * @param size     dimensione pagina richiesta
     * @param maxSize  dimensione massima consentita
     * @param sort     ordinamento (se {@code null}, viene usato {@link Sort#unsorted()})
     */
    public static Pageable pageRequest(int page, int size, int maxSize, Sort sort) {
        int safePage = Math.max(page, MIN_PAGE);
        int safeSize = Math.min(Math.max(size, MIN_SIZE), maxSize);
        Sort safeSort = Objects.nonNull(sort) ? sort : Sort.unsorted();

        return PageRequest.of(safePage, safeSize, safeSort);
    }
}
