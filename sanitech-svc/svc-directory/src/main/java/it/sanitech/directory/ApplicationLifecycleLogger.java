package it.sanitech.directory;

import it.sanitech.directory.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Component;

/**
 * Logger di ciclo di vita applicativo.
 *
 * <p>
 * Centralizza log "startup/shutdown" con informazioni essenziali (nome applicazione, porta).
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationLifecycleLogger {

    private final Environment env;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        String appName = env.getProperty(AppConstants.Spring.APP_NAME_KEY, AppConstants.Spring.DEFAULT_APP_NAME);
        String port = env.getProperty(AppConstants.Spring.SERVER_PORT_KEY, AppConstants.Spring.DEFAULT_SERVER_PORT);

        log.info("Microservizio {} avviato correttamente sulla porta {}.", appName, port);
    }

    @EventListener(ContextClosedEvent.class)
    public void onShutdown() {
        String appName = env.getProperty(AppConstants.Spring.APP_NAME_KEY, AppConstants.Spring.DEFAULT_APP_NAME);
        log.warn("Arresto del microservizio {} in corso…", appName);
    }
}
