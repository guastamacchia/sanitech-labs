package it.sanitech.gateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Dedupe dei header CORS in risposta per evitare valori duplicati
 * (es. Access-Control-Allow-Origin). Utile quando gateway e servizi
 * downstream applicano entrambi CORS.
 */
@Component
public class CorsResponseHeaderDedupFilter implements GlobalFilter, Ordered {

    private static final String HEADER_ALLOW_ORIGIN = HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
    private static final String HEADER_ALLOW_CREDENTIALS = HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    HttpHeaders headers = exchange.getResponse().getHeaders();
                    dedupeHeader(headers, HEADER_ALLOW_ORIGIN);
                    dedupeHeader(headers, HEADER_ALLOW_CREDENTIALS);
                }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private static void dedupeHeader(HttpHeaders headers, String headerName) {
        List<String> values = headers.get(headerName);
        if (values == null || values.isEmpty()) {
            return;
        }

        Set<String> unique = new LinkedHashSet<>();
        for (String value : values) {
            if (value == null) {
                continue;
            }
            for (String token : value.split(",")) {
                String trimmed = token.trim();
                if (!trimmed.isEmpty()) {
                    unique.add(trimmed);
                }
            }
        }

        if (unique.isEmpty()) {
            return;
        }

        if (unique.size() == 1) {
            headers.set(headerName, unique.iterator().next());
        } else {
            headers.put(headerName, new ArrayList<>(unique));
        }
    }
}
