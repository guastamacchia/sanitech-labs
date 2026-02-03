package it.sanitech.outbox.core;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Objects;

/**
 * Informazioni sull'attore che ha generato un evento.
 * <p>
 * Utilizzato per tracciare chi ha eseguito un'azione nel sistema di audit.
 * </p>
 *
 * @param actorType tipo di attore (es. ADMIN, DOCTOR, PATIENT, SYSTEM)
 * @param actorId   identificativo univoco dell'attore (es. email, userId)
 * @param actorName nome visualizzabile dell'attore (opzionale)
 */
public record ActorInfo(
        String actorType,
        String actorId,
        String actorName
) {

    /**
     * Attore di sistema per operazioni automatiche.
     */
    public static final ActorInfo SYSTEM = new ActorInfo("SYSTEM", "system", "Sistema");

    /**
     * Crea ActorInfo da un oggetto Authentication Spring Security.
     * <p>
     * Estrae automaticamente:
     * <ul>
     *   <li>actorId: dal principal name (tipicamente email o username)</li>
     *   <li>actorType: dal primo ruolo trovato (ROLE_ADMIN -> ADMIN, ROLE_DOCTOR -> DOCTOR, etc.)</li>
     *   <li>actorName: uguale ad actorId se non disponibile altro</li>
     * </ul>
     * </p>
     *
     * @param auth oggetto Authentication da cui estrarre le informazioni
     * @return ActorInfo popolato, o SYSTEM se auth è null
     */
    public static ActorInfo from(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return SYSTEM;
        }

        String actorId = auth.getName();
        String actorType = resolveActorType(auth.getAuthorities());
        String actorName = actorId; // Potrebbe essere esteso per estrarre nome dal JWT

        return new ActorInfo(actorType, actorId, actorName);
    }

    /**
     * Crea ActorInfo con valori espliciti.
     *
     * @param actorType tipo attore
     * @param actorId   identificativo attore
     * @return ActorInfo
     */
    public static ActorInfo of(String actorType, String actorId) {
        return new ActorInfo(
                Objects.requireNonNull(actorType, "actorType obbligatorio"),
                Objects.requireNonNull(actorId, "actorId obbligatorio"),
                actorId
        );
    }

    /**
     * Crea ActorInfo con valori espliciti incluso nome.
     *
     * @param actorType tipo attore
     * @param actorId   identificativo attore
     * @param actorName nome visualizzabile
     * @return ActorInfo
     */
    public static ActorInfo of(String actorType, String actorId, String actorName) {
        return new ActorInfo(
                Objects.requireNonNull(actorType, "actorType obbligatorio"),
                Objects.requireNonNull(actorId, "actorId obbligatorio"),
                actorName
        );
    }

    private static String resolveActorType(Collection<? extends GrantedAuthority> authorities) {
        if (authorities == null || authorities.isEmpty()) {
            return "USER";
        }

        for (GrantedAuthority authority : authorities) {
            String auth = authority.getAuthority();
            if (auth == null) continue;

            // Priorità: ADMIN > DOCTOR > PATIENT > USER
            if (auth.contains("ADMIN")) {
                return "ADMIN";
            }
            if (auth.contains("DOCTOR")) {
                return "DOCTOR";
            }
            if (auth.contains("PATIENT")) {
                return "PATIENT";
            }
        }

        return "USER";
    }

    /**
     * Verifica se questo attore è di tipo SYSTEM.
     */
    public boolean isSystem() {
        return "SYSTEM".equals(actorType);
    }
}
