package it.sanitech.directory.repositories.spec;

import it.sanitech.directory.repositories.entities.Department;
import it.sanitech.directory.repositories.entities.Doctor;
import it.sanitech.directory.repositories.entities.Specialization;
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
 * specializzazione, mantenendo la query distinct per gestire le join many-to-many.
 * </p>
 */
public final class DoctorSpecifications {

    private DoctorSpecifications() {}

    public static Specification<Doctor> search(String q, String departmentCode, String specializationCode) {
        return (root, query, cb) -> {
            query.distinct(true);

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
                Join<Doctor, Department> dep = root.join("departments", JoinType.INNER);
                predicates.add(cb.equal(cb.upper(dep.get("code")), departmentCode.trim().toUpperCase()));
            }

            if (specializationCode != null && !specializationCode.isBlank()) {
                Join<Doctor, Specialization> spec = root.join("specializations", JoinType.INNER);
                predicates.add(cb.equal(cb.upper(spec.get("code")), specializationCode.trim().toUpperCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
