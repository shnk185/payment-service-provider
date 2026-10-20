package org.ngs.paymentserviceprovider.repository;

import org.ngs.paymentserviceprovider.entity.WebhookDeliveryEntity;
import org.ngs.paymentserviceprovider.enums.WebhookDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface WebhookDeliveryRepository extends JpaRepository<WebhookDeliveryEntity, Long> {

    @Query(value = "select * from webhook_deliveries where status = 'PENDING' "
            + "and next_attempt_at <= :now "
            + "order by next_attempt_at limit :batchSize for update skip locked", nativeQuery = true)
    List<WebhookDeliveryEntity> findDueForUpdate(@Param("batchSize") int batchSize,
                                                 @Param("now") Instant now);

    long deleteByStatusAndDeliveredAtBefore(WebhookDeliveryStatus status, Instant cutoff);
}
