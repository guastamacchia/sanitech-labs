package it.sanitech.directory.repositories.spec;

import it.sanitech.directory.repositories.entities.Department;
import it.sanitech.directory.repositories.entities.Patient;
import it.sanitech.directory.repositories.entities.UserStatus;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Specifiche JPA per la ricerca di {@link Patient}.
 *
 * <p>
 * Fornisce filtri componibili per ricerca testuale, vincoli di reparto e stato,
 * con gestione di query distinct per evitare duplicazioni dovute alle join.
 * </p>
 */
public final class PatientSpecifications {

    private PatientSpecifications() {}

    public static Specification<Patient> search(String q, String departmentCode, UserStatus status) {
        return (root, query, cb) -> {
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), like),
                        cb.like(cb.lower(root.get("lastName")), like),
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(cb.lower(root.get("fiscalCode")), like)
                ));
            }

            if (departmentCode != null && !departmentCode.isBlank()) {
                Join<Patient, Department> dep = root.join("departments", JoinType.INNER);
                predicates.add(cb.equal(cb.upper(dep.get("code")), departmentCode.trim().toUpperCase()));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filtro per uno o più reparti (IN su {@code departments.code}).
     * Utile per applicare ABAC su endpoint di lettura destinati ai DOCTOR.
     */
    public static Specification<Patient> inDepartments(Set<String> departmentCodes) {
        return (root, query, cb) -> {
            query.distinct(true);
            if (departmentCodes == null || departmentCodes.isEmpty()) {
                // Nessun reparto → nessun risultato (policy conservative)
                return cb.disjunction();
            }
            Join<Patient, Department> dep = root.join("departments", JoinType.INNER);
            return dep.get("code").in(departmentCodes);
        };
    }
}
