package it.sanitech.payments.utilities;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro che gestisce un {@code traceId} per correlare i log della singola richiesta.
 *
 * <p>
 * Se il client invia {@code X-Request-Id}, viene riutilizzato; altrimenti viene generato.
 * Il valore viene propagato anche nella response.
 * </p>
 */
@Component
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String MDC_KEY_TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = request.getHeader(AppConstants.Headers.X_REQUEST_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        MDC.put(MDC_KEY_TRACE_ID, traceId);
        response.setHeader(AppConstants.Headers.X_REQUEST_ID, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY_TRACE_ID);
        }
    }
}
