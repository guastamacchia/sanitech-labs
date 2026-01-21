package it.sanitech.outbox.utilities;

/**
 * Costanti interne al modulo Outbox.
 * Scopo: evitare dipendenze verso altre librerie (es. commons-libraries) solo per costanti/prefix.
 */
public final class OutboxConstants {

    private OutboxConstants() {}

    public static final String CONFIG_PREFIX = "sanitech.outbox";
    public static final String PUBLISHER_PREFIX = CONFIG_PREFIX + ".publisher";

    public static final String DEFAULT_TOPIC = "domain-events";
}
