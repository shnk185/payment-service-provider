package org.ngs.paymentserviceprovider.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngs.paymentserviceprovider.entity.OutboxEventEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {
    private final OutboxService outboxService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${outbox.batch-size:50}")
    private int batchSize;

    @Value("${outbox.send-timeout-seconds:10}")
    private int sendTimeoutSeconds;

    @Value("${outbox.claim-stale-seconds:60}")
    private int claimStaleSeconds;

    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms:1000}")
    public void publishPending() {
        List<OutboxEventEntity> events = outboxService.claimBatch(batchSize, claimStaleSeconds);
        for (OutboxEventEntity event : events) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload())
                        .get(sendTimeoutSeconds, TimeUnit.SECONDS);
                outboxService.markPublished(event);
            } catch (Exception e) {
                log.warn("could not publish outbox event {} to {}", event.getId(), event.getTopic(), e);
                outboxService.markFailed(event, e.getMessage());
            }
        }
    }

    @Scheduled(cron = "${outbox.purge-cron:0 0 * * * *}")
    public void purgePublished() {
        outboxService.purgePublished();
    }
}