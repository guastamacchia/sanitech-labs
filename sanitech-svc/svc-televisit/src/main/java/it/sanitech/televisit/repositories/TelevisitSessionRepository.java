package it.sanitech.televisit.repositories;

import it.sanitech.televisit.repositories.entities.TelevisitSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repository per {@link TelevisitSession}.
 *
 * <p>Per ricerche flessibili (filtri multipli) viene usato {@link JpaSpecificationExecutor}.</p>
 */
public interface TelevisitSessionRepository extends JpaRepository<TelevisitSession, Long>, JpaSpecificationExecutor<TelevisitSession> {

    boolean existsByRoomName(String roomName);
}
