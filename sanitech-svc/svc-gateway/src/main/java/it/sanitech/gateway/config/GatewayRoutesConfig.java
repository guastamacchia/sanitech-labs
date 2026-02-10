package it.sanitech.gateway.config;

import it.sanitech.gateway.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

/**
 * Definisce le rotte del gateway verso i microservizi Sanitech.
 *
 * <p>
 * La configurazione è in Java (e non solo in YAML) per:
 * <ul>
 *   <li>avere tipizzazione e refactoring sicuro;</li>
 *   <li>centralizzare filtri comuni (CircuitBreaker/Retry);</li>
 *   <li>mantenere un punto unico di “governo” delle route.</li>
 * </ul>
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class GatewayRoutesConfig {

    private static final HttpMethod[] IDEMPOTENT_METHODS =
        { HttpMethod.GET, HttpMethod.HEAD };


    private final GatewayServicesProperties services;

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()

                // =========================
                // Public endpoints (no auth)
                // =========================
                .route("public-directory", r -> r
                        .path("/api/public/**")
                        .filters(f -> standardFilters(f, AppConstants.Services.DIRECTORY))
                        .uri(services.getDirectory())
                )

                // =========================
                // Admissions (ricoveri / posti letto / capacità reparti)
                // NOTA: deve precedere Directory per evitare che /api/departments/**
                //       catturi /api/departments/capacity/**
                // =========================
                .route(AppConstants.Services.ADMISSIONS, r -> r
                        .path("/api/admissions/**", "/api/beds/**", "/api/departments/capacity", "/api/departments/capacity/**")
                        .filters(f -> standardFilters(f, AppConstants.Services.ADMISSIONS))
                        .uri(services.getAdmissions())
                )

                // =========================
                // Televisit
                // NOTA: deve precedere Directory per evitare che /api/patient/**
                //       catturi /api/patient/televisits/**
                // =========================
                .route(AppConstants.Services.TELEVISIT, r -> r
                        .path("/api/televisits/**", "/api/patient/televisits/**", "/api/admin/televisits/**")
                        .filters(f -> standardFilters(f, AppConstants.Services.TELEVISIT))
                        .uri(services.getTelevisit())
                )

                // =========================
                // Prescribing
                // NOTA: deve precedere Directory per evitare che /api/doctor/**
                //       catturi /api/doctor/prescriptions/**
                // =========================
                .route(AppConstants.Services.PRESCRIBING, r -> r
                        .path("/api/prescriptions/**", "/api/prescribing/**",
                              "/api/doctor/prescriptions/**", "/api/admin/prescriptions/**")
                        .filters(f -> standardFilters(f, AppConstants.Services.PRESCRIBING))
                        .uri(services.getPrescribing())
                )

                // =========================
                // Directory (medici/pazienti)
                // =========================
                .route(AppConstants.Services.DIRECTORY, r -> r
                        .path(
                                "/api/doctors/**",
                                "/api/doctor/**",
                                "/api/patients/**",
                                "/api/patient/**",
                                "/api/departments/**",
                                "/api/facilities/**",
                                "/api/specialities/**",
                                "/api/specializations/**",
                                "/api/admin/doctors/**",
                                "/api/admin/patients/**",
                                "/api/admin/departments/**",
                                "/api/admin/facilities/**",
                                "/api/admin/specializations/**"
                        )
                        .filters(f -> standardFilters(f, AppConstants.Services.DIRECTORY))
                        .uri(services.getDirectory())
                )

                // =========================
                // Scheduling (agenda / appuntamenti)
                // =========================
                .route(AppConstants.Services.SCHEDULING, r -> r
                        .path("/api/appointments/**", "/api/slots/**", "/api/scheduling/**", "/api/admin/slots/**")
                        .filters(f -> standardFilters(f, AppConstants.Services.SCHEDULING))
                        .uri(services.getScheduling())
                )

                // =========================
                // Consents (consensi clinici)
                // =========================
                .route(AppConstants.Services.CONSENTS, r -> r
                        .path("/api/consents/**")
                        .filters(f -> standardFilters(f, AppConstants.Services.CONSENTS))
                        .uri(services.getConsents())
                )

                // =========================
                // Docs (documenti clinici)
                // =========================
                .route(AppConstants.Services.DOCS, r -> r
                        .path("/api/docs/**")
                        .filters(f -> standardFilters(f, AppConstants.Services.DOCS))
                        .uri(services.getDocs())
                )

                // =========================
                // Notifications
                // =========================
                .route(AppConstants.Services.NOTIFICATIONS, r -> r
                        .path("/api/notifications/**", "/api/admin/notifications/**")
                        .filters(f -> standardFilters(f, AppConstants.Services.NOTIFICATIONS))
                        .uri(services.getNotifications())
                )

                // =========================
                // Audit
                // =========================
                .route(AppConstants.Services.AUDIT, r -> r
                        .path("/api/audit/**")
                        .filters(f -> standardFilters(f, AppConstants.Services.AUDIT))
                        .uri(services.getAudit())
                )

                // =========================
                // Payments
                // =========================
                .route(AppConstants.Services.PAYMENTS, r -> r
                        .path("/api/payments/**", "/api/admin/payments/**", "/api/admin/services/**")
                        .filters(f -> standardFilters(f, AppConstants.Services.PAYMENTS))
                        .uri(services.getPayments())
                )

                .build();
    }

    /**
     * Filtri standard applicati a tutte le route.
     *
     * <p>
     * Include:
     * <ul>
     *   <li>Circuit breaker con fallback locale (risposta 503 “controllata”);</li>
     *   <li>Retry limitato a chiamate idempotenti (GET/HEAD), per ridurre errori transitori.</li>
     * </ul>
     * </p>
     */
    private GatewayFilterSpec standardFilters(GatewayFilterSpec f, String serviceId) {
        return f
                .circuitBreaker(cb -> cb
                        .setName(serviceId)
                        .setFallbackUri("forward:/fallback/" + serviceId)
                )
                .retry(retry -> retry
                        .setRetries(2)
                        .setMethods(IDEMPOTENT_METHODS)
                        .setStatuses(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                                org.springframework.http.HttpStatus.BAD_GATEWAY,
                                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                                org.springframework.http.HttpStatus.GATEWAY_TIMEOUT)
                );
    }
}
