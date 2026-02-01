package it.sanitech.audit.ingestion;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurazione per l'ingestion degli eventi audit da Kafka.
 */
@ConfigurationProperties(prefix = "sanitech.audit.ingestion")
public class AuditIngestionProperties {

    /**
     * Abilita il consumer di ingestion.
     */
    private boolean enabled = true;

    /**
     * Lista di topic Kafka separati da virgola.
     */
    private String topics;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTopics() {
        return topics;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }
}
