package it.sanitech.consents;

import it.sanitech.consents.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Log essenziali del ciclo di vita applicativo.
 * <p>
 * In produzione questi messaggi aiutano a correlare start/stop del pod con gli eventi del cluster.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationLifecycleLogger {

    private final Environment env;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        String appName = env.getProperty(AppConstants.Spring.APP_NAME_KEY, AppConstants.Spring.APP_NAME_DEFAULT);
        String port = env.getProperty(AppConstants.Spring.SERVER_PORT_KEY, AppConstants.Spring.SERVER_PORT_DEFAULT);
        log.info("Avvio completato: {} in ascolto sulla porta {}", appName, port);
    }

    @EventListener(ContextClosedEvent.class)
    public void onStopping() {
        log.warn("Arresto del microservizio in corso…");
    }
}
