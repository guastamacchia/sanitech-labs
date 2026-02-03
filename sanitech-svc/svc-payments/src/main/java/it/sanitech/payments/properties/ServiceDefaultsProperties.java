package it.sanitech.payments.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione importi default per le prestazioni sanitarie.
 */
@Configuration
@ConfigurationProperties(prefix = "sanitech.payments.service-defaults")
@Getter
@Setter
public class ServiceDefaultsProperties {

    /**
     * Importo default per una visita medica in centesimi (default: 10000 = 100 EUR).
     */
    private long medicalVisitAmountCents = 10000L;

    /**
     * Importo default per un giorno di ricovero in centesimi (default: 2000 = 20 EUR).
     */
    private long hospitalizationDailyAmountCents = 2000L;
}
