package org.ngs.paymentserviceprovider.repository;

import org.ngs.paymentserviceprovider.entity.CheckoutSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CheckoutSessionRepository extends JpaRepository<CheckoutSessionEntity, Long> {
    Optional<CheckoutSessionEntity> findByIdempotencyKey(String idempotencyKey);
}
