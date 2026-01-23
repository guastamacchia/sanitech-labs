package it.sanitech.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Proprietà che definiscono le URL base dei microservizi downstream.
 *
 * <p>
 * Le URL sono configurate in {@code application.yml} sotto {@code sanitech.gateway.services.*}
 * e possono essere sovrascritte via variabili d'ambiente (es. {@code DIRECTORY_URL}).
 * </p>
 */
@Data
@ConfigurationProperties(prefix = "sanitech.gateway.services")
public class GatewayServicesProperties {

    /** Base URL di {@code svc-directory}. */
    private String directory;

    /** Base URL di {@code svc-scheduling}. */
    private String scheduling;

    /** Base URL di {@code svc-admissions}. */
    private String admissions;

    /** Base URL di {@code svc-consents}. */
    private String consents;

    /** Base URL di {@code svc-docs}. */
    private String docs;

    /** Base URL di {@code svc-notifications}. */
    private String notifications;

    /** Base URL di {@code svc-audit}. */
    private String audit;

    /** Base URL di {@code svc-televisit}. */
    private String televisit;

    /** Base URL di {@code svc-payments}. */
    private String payments;

    /** Base URL di {@code svc-prescribing}. */
    private String prescribing;
}
