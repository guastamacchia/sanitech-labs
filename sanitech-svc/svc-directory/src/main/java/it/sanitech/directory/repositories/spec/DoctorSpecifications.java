package it.sanitech.directory.repositories.spec;

import it.sanitech.directory.repositories.entities.Department;
import it.sanitech.directory.repositories.entities.Doctor;
import it.sanitech.directory.repositories.entities.Facility;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Specifiche JPA per la ricerca di {@link Doctor} con filtri combinabili.
 *
 * <p>
 * Produce {@link Specification} componibili per nome/cognome/email e per filtri su reparto e
 * struttura di appartenenza.
 * Gerarchia: Struttura -> Reparto -> Medico.
 * </p>
 */
public final class DoctorSpecifications {

    private DoctorSpecifications() {}

    public static Specification<Doctor> search(String q, String departmentCode, String facilityCode) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), like),
                        cb.like(cb.lower(root.get("lastName")), like),
                        cb.like(cb.lower(root.get("email")), like)
                ));
            }

            if (departmentCode != null && !departmentCode.isBlank()) {
                Join<Doctor, Department> dep = root.join("department", JoinType.INNER);
                predicates.add(cb.equal(cb.upper(dep.get("code")), departmentCode.trim().toUpperCase()));
            }

            if (facilityCode != null && !facilityCode.isBlank()) {
                Join<Doctor, Department> dep = root.join("department", JoinType.INNER);
                Join<Department, Facility> fac = dep.join("facility", JoinType.INNER);
                predicates.add(cb.equal(cb.upper(fac.get("code")), facilityCode.trim().toUpperCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
