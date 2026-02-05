package it.sanitech.televisit.services.livekit;

import it.sanitech.televisit.config.LiveKitProperties;
import it.sanitech.televisit.services.dto.LiveKitTokenDto;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Generazione token LiveKit per l'accesso alle room.
 */
@Service
@RequiredArgsConstructor
public class LiveKitTokenService {

    private final LiveKitProperties props;

    public LiveKitTokenDto createRoomJoinToken(String roomName, String identity, String displayName) {
        AccessToken token = new AccessToken(props.getApiKey(), props.getApiSecret());
        token.setIdentity(identity);
        token.setName(displayName);

        // Permesso di join alla room specifica.
        token.addGrants(new RoomJoin(true), new RoomName(roomName));

        String jwt = token.toJwt();

        // Restituisce l'URL WebSocket per il frontend (connessione WebRTC)
        return new LiveKitTokenDto(roomName, props.getWsUrl(), jwt, props.getTokenTtlSeconds());
    }
}
