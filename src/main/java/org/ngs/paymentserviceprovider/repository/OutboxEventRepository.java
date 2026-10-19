package org.ngs.paymentserviceprovider.repository;

import org.ngs.paymentserviceprovider.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {

    @Query(value = "select * from outbox_events where published = false "
            + "and (claimed_at is null or claimed_at < :staleBefore) "
            + "order by id limit :batchSize for update skip locked", nativeQuery = true)
    List<OutboxEventEntity> findUnpublishedForUpdate(@Param("batchSize") int batchSize,
                                                    @Param("staleBefore") Instant staleBefore);

    long deleteByPublishedTrueAndPublishedAtBefore(Instant cutoff);
}