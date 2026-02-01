package it.sanitech.prescribing;

import it.sanitech.prescribing.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Logger di lifecycle dell'applicazione.
 *
 * <p>
 * È utile in produzione per avere un segnale chiaro di avvio completato e di shutdown,
 * soprattutto in ambienti containerizzati (Kubernetes).
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationLifecycleLogger {

    private final Environment env;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        String name = env.getProperty("spring.application.name", AppConstants.DefaultValues.App.DEFAULT_APP_NAME);
        String port = env.getProperty("server.port", AppConstants.DefaultValues.App.DEFAULT_PORT);
        log.info("Microservizio {} avviato correttamente. Port={}", name, port);
    }

    @EventListener(ContextClosedEvent.class)
    public void onShutdown() {
        log.warn("Arresto del microservizio Sanitech Prescribing in corso…");
    }
}
