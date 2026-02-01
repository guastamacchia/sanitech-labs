package it.sanitech.outbox.autoconfigure;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Properties della libreria Outbox.
 *
 * Nota: le annotazioni di validazione sono attive se nel classpath è presente Bean Validation
 * (spring-boot-starter-validation).
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "sanitech.outbox")
public class OutboxProperties {

    /**
     * Abilitazione globale della libreria.
     */
    private boolean enabled = true;

    @Valid
    private Publisher publisher = new Publisher();

    @Getter
    @Setter
    public static class Publisher {

        /**
         * Abilita il job schedulato che pubblica su Kafka.
         */
        private boolean enabled = false;

        /**
         * Topic Kafka di destinazione.
         */
        @NotBlank
        private String topic = "domain-events";

        /**
         * Numero massimo di eventi per ciclo.
         */
        @Min(1)
        private int batchSize = 100;

        /**
         * Ritardo fisso tra un ciclo e l'altro (ms).
         */
        @Min(100)
        private long fixedDelayMs = 2000;

        /**
         * Timeout massimo per attesa ACK Kafka (ms).
         */
        @Min(100)
        private long sendTimeoutMs = 5000;
    }
}
