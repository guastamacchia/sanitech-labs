package it.sanitech.commons.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation per marcare i metodi dei controller che devono essere auditati automaticamente.
 * <p>
 * L'AuditAspect intercetta i metodi annotati e pubblica un evento di audit
 * sulla coda Kafka {@code audits.events} tramite il pattern Outbox.
 * </p>
 *
 * <p>Esempio di utilizzo:</p>
 * <pre>{@code
 * @PostMapping
 * @Auditable(aggregateType = "PATIENT", eventType = "PATIENT_CREATED", aggregateIdSpel = "id")
 * public PatientDto create(@RequestBody CreatePatientRequest request) {
 *     return patientService.create(request);
 * }
 *
 * @DeleteMapping("/{id}")
 * @Auditable(aggregateType = "PATIENT", eventType = "PATIENT_DELETED", aggregateIdParam = "id")
 * public void delete(@PathVariable Long id) {
 *     patientService.delete(id);
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /**
     * Tipo di aggregato (es. "PATIENT", "DOCTOR", "ADMISSION").
     * <p>
     * Identifica la risorsa principale coinvolta nell'operazione.
     * </p>
     */
    String aggregateType();

    /**
     * Tipo di evento (es. "PATIENT_CREATED", "DOCTOR_DELETED").
     * <p>
     * Descrive l'azione eseguita sulla risorsa.
     * </p>
     */
    String eventType();

    /**
     * Nome del parametro del metodo da usare come aggregateId.
     * <p>
     * Utilizzare per operazioni su risorse esistenti (update, delete)
     * dove l'ID è passato come parametro.
     * </p>
     * <p>
     * Mutualmente esclusivo con {@link #aggregateIdSpel()}.
     * </p>
     *
     * <p>Esempio:</p>
     * <pre>{@code
     * @DeleteMapping("/{id}")
     * @Auditable(aggregateType = "PATIENT", eventType = "PATIENT_DELETED", aggregateIdParam = "id")
     * public void delete(@PathVariable Long id) { ... }
     * }</pre>
     */
    String aggregateIdParam() default "";

    /**
     * Espressione SpEL per estrarre aggregateId dal return value.
     * <p>
     * Utilizzare per operazioni di creazione dove l'ID viene generato
     * e restituito nel DTO di risposta.
     * </p>
     * <p>
     * Mutualmente esclusivo con {@link #aggregateIdParam()}.
     * </p>
     *
     * <p>Esempi:</p>
     * <ul>
     *   <li>{@code "id"} - accede al campo {@code id} del return value</li>
     *   <li>{@code "getId()"} - invoca il getter</li>
     *   <li>{@code "patient.id"} - accede a campo annidato</li>
     *   <li>{@code "size()"} - per liste (bulk operations)</li>
     * </ul>
     */
    String aggregateIdSpel() default "";

    /**
     * Se true, include il return value come payload dell'evento.
     * <p>
     * Default false per motivi di performance e privacy.
     * Usare con cautela per evitare di loggare dati sensibili.
     * </p>
     */
    boolean includePayload() default false;

    /**
     * Campi del return value da includere nel payload (espressioni SpEL).
     * <p>
     * Utilizzato solo se {@link #includePayload()} = true.
     * Permette di selezionare solo alcuni campi invece dell'intero oggetto.
     * </p>
     *
     * <p>Esempio:</p>
     * <pre>{@code
     * @Auditable(..., includePayload = true, payloadFields = {"id", "email", "status"})
     * }</pre>
     */
    String[] payloadFields() default {};

    /**
     * Topic Kafka di destinazione.
     * <p>
     * Default: {@code audits.events}
     * </p>
     */
    String topic() default "audits.events";
}
