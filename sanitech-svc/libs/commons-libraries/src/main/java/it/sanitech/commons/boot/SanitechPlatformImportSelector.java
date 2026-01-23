package it.sanitech.commons.boot;

import it.sanitech.commons.utilities.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Selettore di import che abilita in modo controllato i componenti delle librerie Sanitech.
 */
@Slf4j
public class SanitechPlatformImportSelector implements ImportSelector, EnvironmentAware {

    /**
     * Nome completo dell'annotation usata per abilitare la piattaforma.
     * Centralizzato per evitare stringhe duplicate.
     */
    private static final String ENABLE_PLATFORM_ANNOTATION =
            EnableSanitechPlatform.class.getName();

    private Environment environment;

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public @NonNull String[] selectImports(@NonNull AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attrs = importingClassMetadata.getAnnotationAttributes(ENABLE_PLATFORM_ANNOTATION, false);

        boolean enableOutbox = true;
        if (attrs != null && attrs.containsKey(AppConstants.ConfigKeys.Outbox.ATTR_ENABLE_OUTBOX)) {
            enableOutbox = (Boolean) attrs.get(AppConstants.ConfigKeys.Outbox.ATTR_ENABLE_OUTBOX);
        }

        List<String> imports = new ArrayList<>();

        // Commons "core"
        imports.add(SanitechCommonsComponentScanConfiguration.class.getName());
        log.debug("Piattaforma Sanitech: abilitato component-scan per commons-libraries (core).");

        // Outbox (opzionale)
        if (enableOutbox) {
            // Nota per junior: abilitiamo l'outbox solo se la libreria è presente nel classpath.
            // Questo evita errori di startup quando un microservizio NON dipende dal modulo outbox.
            if (isClassPresent()) {
                imports.add(AppConstants.ConfigKeys.Outbox.OUTBOX_SCAN_CONFIGURATION_FQCN);
                log.debug("Piattaforma Sanitech: abilitato component-scan per outbox (config trovata nel classpath).");
            } else {
                log.debug("Piattaforma Sanitech: outbox non presente nel classpath (nessuna config di scan trovata).");
            }
        } else {
            log.debug("Piattaforma Sanitech: outbox disabilitato via annotation.");
        }

        // Log di contesto
        if (Objects.nonNull(environment)) {
            log.debug("Piattaforma Sanitech: profili attivi = {}", (Object) environment.getActiveProfiles());
        }

        return imports.toArray(String[]::new);
    }

    /**
     * Verifica la presenza di una classe nel classpath senza inizializzarla.
     */
    private boolean isClassPresent() {
        try {
            // Usiamo Class.forName con initialize=false per un check "leggero":
            // non vogliamo eseguire blocchi statici o side-effect durante la sola verifica.
            Class.forName(AppConstants.ConfigKeys.Outbox.OUTBOX_SCAN_CONFIGURATION_FQCN, false, getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
