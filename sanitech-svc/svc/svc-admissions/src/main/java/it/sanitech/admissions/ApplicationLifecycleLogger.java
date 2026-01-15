package it.sanitech.admissions;

import it.sanitech.admissions.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Logger di lifecycle applicativo.
 *
 * <p>
 * Scrive log sintetici ad avvio e arresto del microservizio, senza
 * introdurre dipendenze da classi evento deprecate/inesistenti.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationLifecycleLogger {

    private final Environment env;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        String appName = env.getProperty(AppConstants.Spring.KEY_APP_NAME, AppConstants.Spring.DEFAULT_APP_NAME);
        String port = env.getProperty(AppConstants.Spring.KEY_SERVER_PORT, AppConstants.Spring.DEFAULT_SERVER_PORT);
        log.info("Avvio completato: {} in ascolto sulla porta {}", appName, port);
    }

    @EventListener(ContextClosedEvent.class)
    public void onStopping() {
        String appName = env.getProperty(AppConstants.Spring.KEY_APP_NAME, AppConstants.Spring.DEFAULT_APP_NAME);
        log.warn("Arresto del microservizio {} in corso…", appName);
    }
}
