package org.ngs.paymentserviceprovider.repository;

import jakarta.persistence.LockModeType;
import org.ngs.paymentserviceprovider.entity.CheckoutSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CheckoutSessionRepository extends JpaRepository<CheckoutSessionEntity, Long> {
    Optional<CheckoutSessionEntity> findByIdempotencyKey(String idempotencyKey);

    Optional<CheckoutSessionEntity> findByUniqueId(String uniqueId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from CheckoutSessionEntity s where s.uniqueId = :uniqueId")
    Optional<CheckoutSessionEntity> findByUniqueIdForUpdate(@Param("uniqueId") String uniqueId);
}
