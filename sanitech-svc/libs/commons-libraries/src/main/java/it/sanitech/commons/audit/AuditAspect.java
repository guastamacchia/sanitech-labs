package it.sanitech.commons.audit;

import it.sanitech.outbox.core.ActorInfo;
import it.sanitech.outbox.core.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspect AOP che intercetta i metodi annotati con {@link Auditable} e pubblica
 * automaticamente eventi di audit sulla coda Kafka.
 * <p>
 * L'aspect:
 * <ul>
 *   <li>Estrae l'utente corrente dal SecurityContext (JWT)</li>
 *   <li>Determina l'aggregateId dai parametri o dal return value</li>
 *   <li>Pubblica l'evento tramite {@link DomainEventPublisher}</li>
 * </ul>
 * </p>
 * <p>
 * L'evento viene pubblicato solo se il metodo completa con successo
 * (usa {@code @AfterReturning}).
 * </p>
 */
@Aspect
@Slf4j
@RequiredArgsConstructor
public class AuditAspect {

    private final DomainEventPublisher eventPublisher;

    private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();

    /**
     * Intercetta i metodi annotati con @Auditable dopo la loro esecuzione con successo.
     *
     * @param joinPoint punto di join AOP
     * @param auditable annotation con i metadati di audit
     * @param result    valore di ritorno del metodo
     */
    @AfterReturning(
            pointcut = "@annotation(auditable)",
            returning = "result"
    )
    public void auditAfterSuccess(JoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            ActorInfo actor = resolveActor(auth);

            String aggregateId = resolveAggregateId(joinPoint, auditable, result);
            Object payload = buildPayload(auditable, result);

            eventPublisher.publish(
                    auditable.aggregateType(),
                    aggregateId,
                    auditable.eventType(),
                    payload,
                    auditable.topic(),
                    actor
            );

            log.debug("Audit: evento {} pubblicato per aggregate {}/{} da {}",
                    auditable.eventType(),
                    auditable.aggregateType(),
                    aggregateId,
                    actor.actorId());

        } catch (Exception e) {
            // Non propagare eccezioni dall'auditing per non impattare la business logic
            log.error("Audit: errore nella pubblicazione evento {}: {}",
                    auditable.eventType(), e.getMessage(), e);
        }
    }

    /**
     * Risolve le informazioni sull'attore dall'Authentication.
     * Se l'utente non è autenticato, restituisce ANONYMOUS.
     */
    private ActorInfo resolveActor(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ActorInfo.ANONYMOUS;
        }

        Object principal = auth.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) {
            return ActorInfo.ANONYMOUS;
        }

        return ActorInfo.from(auth);
    }

    /**
     * Risolve l'aggregateId basandosi sulla configurazione dell'annotation.
     * Priorità: aggregateIdParam > aggregateIdSpel > fallback "id" su result > "UNKNOWN"
     */
    private String resolveAggregateId(JoinPoint joinPoint, Auditable auditable, Object result) {
        // 1. Prova dal parametro del metodo
        if (!auditable.aggregateIdParam().isEmpty()) {
            String value = extractFromParameter(joinPoint, auditable.aggregateIdParam());
            if (value != null) {
                return value;
            }
        }

        // 2. Prova con SpEL sul return value
        if (!auditable.aggregateIdSpel().isEmpty() && result != null) {
            String value = extractWithSpel(result, auditable.aggregateIdSpel());
            if (value != null) {
                return value;
            }
        }

        // 3. Fallback: prova "id" sul result
        if (result != null) {
            String value = extractWithSpel(result, "id");
            if (value != null) {
                return value;
            }
        }

        // 4. Ultimo fallback
        log.warn("Audit: impossibile determinare aggregateId per evento {}", auditable.eventType());
        return "UNKNOWN";
    }

    /**
     * Estrae un valore da un parametro del metodo.
     */
    private String extractFromParameter(JoinPoint joinPoint, String paramName) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Parameter[] parameters = method.getParameters();
            Object[] args = joinPoint.getArgs();

            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].getName().equals(paramName)) {
                    Object value = args[i];
                    return value != null ? String.valueOf(value) : null;
                }
            }

            // Fallback: prova con i nomi dei parametri dalla signature
            String[] paramNames = signature.getParameterNames();
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    if (paramNames[i].equals(paramName)) {
                        Object value = args[i];
                        return value != null ? String.valueOf(value) : null;
                    }
                }
            }

        } catch (Exception e) {
            log.warn("Audit: errore estrazione parametro '{}': {}", paramName, e.getMessage());
        }
        return null;
    }

    /**
     * Estrae un valore da un oggetto usando SpEL.
     */
    private String extractWithSpel(Object target, String spelExpression) {
        try {
            Expression expression = SPEL_PARSER.parseExpression(spelExpression);
            EvaluationContext context = new StandardEvaluationContext(target);
            Object value = expression.getValue(context);
            return value != null ? String.valueOf(value) : null;
        } catch (Exception e) {
            log.debug("Audit: errore SpEL '{}' su {}: {}", spelExpression, target.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * Costruisce il payload dell'evento.
     * Se includePayload=false, restituisce un Map vuoto.
     * Se payloadFields è specificato, include solo quei campi.
     */
    private Object buildPayload(Auditable auditable, Object result) {
        if (!auditable.includePayload() || result == null) {
            return Map.of();
        }

        String[] fields = auditable.payloadFields();
        if (fields.length == 0) {
            // Include l'intero result
            return result;
        }

        // Include solo i campi specificati
        Map<String, Object> payload = new HashMap<>();
        for (String field : fields) {
            try {
                Expression expression = SPEL_PARSER.parseExpression(field);
                EvaluationContext context = new StandardEvaluationContext(result);
                Object value = expression.getValue(context);
                payload.put(field, value);
            } catch (Exception e) {
                log.debug("Audit: errore estrazione campo '{}' per payload: {}", field, e.getMessage());
            }
        }
        return payload;
    }
}
