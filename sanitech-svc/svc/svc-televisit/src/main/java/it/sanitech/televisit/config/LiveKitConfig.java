package it.sanitech.televisit.config;

import io.livekit.server.RoomServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione bean per l'integrazione LiveKit (Room Service Client).
 */
@Configuration
@RequiredArgsConstructor
public class LiveKitConfig {

    private final LiveKitProperties props;

    @Bean
    public RoomServiceClient roomServiceClient() {
        return RoomServiceClient.createClient(props.getUrl(), props.getApiKey(), props.getApiSecret());
    }
}
