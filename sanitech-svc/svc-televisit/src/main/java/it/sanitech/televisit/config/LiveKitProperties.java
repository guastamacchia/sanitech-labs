package it.sanitech.televisit.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Proprietà di integrazione LiveKit.
 *
 * <p>Fonte: {@code sanitech.livekit.*} in {@code application.yml}.</p>
 */
@Data
@Validated
@ConfigurationProperties(prefix = "sanitech.livekit")
public class LiveKitProperties {

    /**
     * URL del LiveKit server (es. {@code http://localhost:7880}).
     */
    @NotBlank
    private String url = "http://localhost:7880";

    /**
     * API key LiveKit (obbligatoria in ambienti reali).
     */
    private String apiKey = "";

    /**
     * API secret LiveKit (obbligatorio in ambienti reali).
     */
    private String apiSecret = "";

    /**
     * TTL del token in secondi (default: 15 minuti).
     */
    private long tokenTtlSeconds = 900;
}
