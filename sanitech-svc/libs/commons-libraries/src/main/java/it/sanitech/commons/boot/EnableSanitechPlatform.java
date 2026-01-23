package it.sanitech.commons.boot;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Abilita in modo standard e centralizzato i componenti "obbligatori" delle librerie Sanitech
 * all'interno di un microservizio.
 *
 * <p>
 * Obiettivo:
 * <ul>
 *   <li>evitare la ripetizione di scanBasePackages in tutti i microservizi</li>
 *   <li>includere solo i package davvero necessari (escludendo le auto-config)</li>
 *   <li>rendere l'abilitazione di moduli opzionali (es. outbox) configurabile</li>
 * </ul>
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SanitechPlatformImportSelector.class)
public @interface EnableSanitechPlatform {

    /**
     * Se true, prova ad abilitare anche i componenti outbox (se presenti nel classpath).
     * Se false, abilita solo i componenti commons.
     */
    boolean enableOutbox() default true;
}
