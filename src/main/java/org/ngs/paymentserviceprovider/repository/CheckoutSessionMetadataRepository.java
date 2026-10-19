package org.ngs.paymentserviceprovider.repository;

import org.ngs.paymentserviceprovider.entity.CheckoutSessionMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckoutSessionMetadataRepository extends JpaRepository<CheckoutSessionMetadataEntity, Long> {
}
