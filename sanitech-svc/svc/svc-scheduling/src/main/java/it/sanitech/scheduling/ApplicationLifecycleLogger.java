package it.sanitech.scheduling;

import it.sanitech.scheduling.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Logger del ciclo di vita applicativo (start/stop) del microservizio.
 * <p>
 * È utile in produzione per correlare eventi di deploy/rollout e per avere
 * un messaggio chiaro di "service up" e "service stopping".
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationLifecycleLogger implements ApplicationListener<ContextClosedEvent> {

    private final Environment env;

    /**
     * Log emesso a startup completato.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        String appName = env.getProperty(AppConstants.Spring.APP_NAME_KEY, AppConstants.Spring.DEFAULT_APP_NAME);
        String port = env.getProperty(AppConstants.Spring.SERVER_PORT_KEY, AppConstants.Spring.DEFAULT_SERVER_PORT);
        log.info("Microservizio {} avviato correttamente (port {}).", appName, port);
    }

    /**
     * Log emesso quando il contesto Spring viene chiuso (shutdown).
     */
    @Override
    public void onApplicationEvent(@NonNull ContextClosedEvent event) {
        String appName = env.getProperty(AppConstants.Spring.APP_NAME_KEY, AppConstants.Spring.DEFAULT_APP_NAME);
        log.warn("Arresto del microservizio {} in corso…", appName);
    }
}
