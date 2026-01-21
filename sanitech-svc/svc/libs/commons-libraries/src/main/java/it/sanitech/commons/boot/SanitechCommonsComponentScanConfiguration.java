package it.sanitech.commons.boot;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Component-scan mirato per i package "core" di commons-libraries.
 *
 * <p>
 * Scansioniamo solo ciò che deve essere disponibile in ogni microservizio:
 * <ul>
 *   <li>exception: handler ed eccezioni condivise</li>
 *   <li>security: converter/guard e utilità di sicurezza</li>
 *   <li>utilities: costanti e helper</li>
 * </ul>
 * </p>
 */
@Configuration
@ComponentScan(basePackages = {
        "it.sanitech.commons.exception",
        "it.sanitech.commons.security",
        "it.sanitech.commons.utilities"
})
public class SanitechCommonsComponentScanConfiguration {
    // nessun bean: qui si definisce solo lo scope del component-scan
}
