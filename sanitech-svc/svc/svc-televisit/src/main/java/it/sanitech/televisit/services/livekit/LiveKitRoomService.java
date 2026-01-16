package it.sanitech.televisit.services.livekit;

import io.livekit.server.RoomServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import retrofit2.Response;

/**
 * Servizio per operazioni amministrative sulla room LiveKit.
 *
 * <p>In molti setup LiveKit può creare la room al primo join; questo servizio permette
 * di crearla preventivamente (best-effort).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LiveKitRoomService {

    private final RoomServiceClient roomServiceClient;

    /**
     * Crea la room se possibile. Se la room esiste già, non è un errore.
     */
    public void ensureRoomExists(String roomName) {
        try {
            Response<?> resp = roomServiceClient.createRoom(roomName).execute();
            if (!resp.isSuccessful()) {
                // 409/400 ecc.: log e proseguiamo (best-effort).
                log.debug("LiveKit createRoom non riuscito (room={}, http={}): {}", roomName, resp.code(), resp.message());
            }
        } catch (Exception e) {
            log.debug("LiveKit createRoom errore (room={}): {}", roomName, e.toString());
        }
    }
}
