package it.sanitech.gateway.config;

import it.sanitech.gateway.utilities.AppConstants;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Global filter che garantisce la presenza di un {@code X-Request-Id}.
 *
 * <p>
 * Se il client non invia un request id, il gateway ne genera uno e lo propaga sia verso downstream
 * (request header) sia verso il client (response header).
 * </p>
 */
@Component
public class RequestIdFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String incomingRequestId =
            exchange.getRequest().getHeaders().getFirst(AppConstants.Http.HEADER_REQUEST_ID);

        final String requestId =
            (incomingRequestId == null || incomingRequestId.isBlank())
                ? UUID.randomUUID().toString()
                : incomingRequestId;

        if (!requestId.equals(incomingRequestId)) {
            exchange = exchange.mutate()
                .request(req -> req.headers(headers ->
                    headers.set(AppConstants.Http.HEADER_REQUEST_ID, requestId)
                ))
                .build();
        }

        // Propagazione verso il client
        exchange.getResponse().getHeaders()
            .set(AppConstants.Http.HEADER_REQUEST_ID, requestId);

        return chain.filter(exchange);
    }


    @Override
    public int getOrder() {
        // early in the chain
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
