package it.sanitech.directory.services.events;

import it.sanitech.directory.integrations.keycloak.KeycloakAdminClient;
import it.sanitech.directory.integrations.keycloak.KeycloakUserSyncRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserSyncListener {

    private final KeycloakAdminClient keycloakAdminClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserSync(KeycloakUserSyncEvent event) {
        try {
            if (event.previousEmail() != null && !event.previousEmail().equalsIgnoreCase(event.email())) {
                keycloakAdminClient.disableUser(event.previousEmail());
            }
            keycloakAdminClient.syncUser(new KeycloakUserSyncRequest(
                    event.email(),
                    event.firstName(),
                    event.lastName(),
                    event.phone(),
                    event.enabled(),
                    event.roleToAssign(),
                    event.aggregateType(),
                    event.aggregateId()
            ));
        } catch (Exception ex) {
            log.error(
                    "DB commit completed but Keycloak sync failed for {} {} (email: {}).",
                    event.aggregateType(),
                    event.aggregateId(),
                    event.email(),
                    ex
            );
        }
    }
}
