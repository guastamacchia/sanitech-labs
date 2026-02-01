package it.sanitech.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry-point del microservizio {@code svc-gateway} della piattaforma Sanitech.
 *
 * <p>
 * Il gateway espone routing verso i microservizi interni e valida i JWT emessi da Keycloak.
 * Le proprietà custom del dominio ({@code sanitech.*}) sono mappate tramite {@link ConfigurationPropertiesScan}.
 * </p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
