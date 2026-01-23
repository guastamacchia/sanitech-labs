package it.sanitech.payments.repositories;

import it.sanitech.payments.repositories.entities.PaymentOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository JPA per {@link PaymentOrder}.
 */
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    Page<PaymentOrder> findByPatientId(Long patientId, Pageable pageable);

    Optional<PaymentOrder> findByIdempotencyKeyIgnoreCase(String idempotencyKey);

    Optional<PaymentOrder> findByProviderIgnoreCaseAndProviderReferenceIgnoreCase(String provider, String providerReference);
}
