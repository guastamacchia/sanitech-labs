package it.sanitech.televisit.repositories.spec;

import it.sanitech.televisit.repositories.entities.TelevisitSession;
import it.sanitech.televisit.repositories.entities.TelevisitStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

/**
 * Utility per costruire {@link Specification} JPA per le ricerche di sessioni.
 */
public final class TelevisitSpecifications {

    private TelevisitSpecifications() {
    }

    public static Specification<TelevisitSession> filter(
            String department,
            TelevisitStatus status,
            String doctorSubject,
            String patientSubject
    ) {
        return (root, query, cb) -> cb.and(
                Optional.ofNullable(department)
                        .filter(s -> !s.isBlank())
                        .map(d -> cb.equal(cb.upper(root.get("department")), d.trim().toUpperCase()))
                        .orElseGet(cb::conjunction),
                Optional.ofNullable(status)
                        .map(st -> cb.equal(root.get("status"), st))
                        .orElseGet(cb::conjunction),
                Optional.ofNullable(doctorSubject)
                        .filter(s -> !s.isBlank())
                        .map(ds -> cb.equal(root.get("doctorSubject"), ds.trim()))
                        .orElseGet(cb::conjunction),
                Optional.ofNullable(patientSubject)
                        .filter(s -> !s.isBlank())
                        .map(ps -> cb.equal(root.get("patientSubject"), ps.trim()))
                        .orElseGet(cb::conjunction)
        );
    }
}
