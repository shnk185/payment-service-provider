package org.ngs.paymentserviceprovider.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngs.paymentserviceprovider.dto.AuthorizePaymentRequest;
import org.ngs.paymentserviceprovider.entity.OutboxEventEntity;
import org.ngs.paymentserviceprovider.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final Integer maxAttempts = 5;

    @Value("${psp.kafka.authorization-topic}")
    private String authorizationTopic;

    @Transactional
    public void appendAuthorizationRequested(AuthorizePaymentRequest request) {
        outboxEventRepository.save(OutboxEventEntity.builder().aggregateId(request.getSessionUniqueId())
                .topic(authorizationTopic).eventType("AUTHORIZATION_REQUESTED")
                .payload(objectMapper.writeValueAsString(request)).published(false).attempts(0).build());
    }

    @Transactional
    public List<OutboxEventEntity> claimBatch(int batchSize, int staleSeconds) {
        Instant now = Instant.now();
        List<OutboxEventEntity> events = outboxEventRepository
                .findUnpublishedForUpdate(batchSize, now.minusSeconds(staleSeconds));
        events.forEach(event -> event.setClaimedAt(now));
        outboxEventRepository.saveAll(events);
        return events;
    }

    @Transactional
    public void markPublished(OutboxEventEntity event) {
        OutboxEventEntity managed = outboxEventRepository.findById(event.getId()).orElseThrow();
        managed.setPublished(true);
        managed.setPublishedAt(Instant.now());
        managed.setLastError(null);
        outboxEventRepository.save(managed);
    }

    @Transactional
    public void markFailed(OutboxEventEntity event, String error) {
        OutboxEventEntity managed = outboxEventRepository.findById(event.getId()).orElseThrow();
        managed.setAttempts(managed.getAttempts() + 1);
        managed.setLastError(error);
        if (!maxAttempts.equals(managed.getAttempts())) {
            managed.setClaimedAt(null);
        }
        outboxEventRepository.save(managed);
    }

    @Transactional
    public void purgePublished() {
        long removed = outboxEventRepository.deleteByPublishedTrueAndPublishedAtBefore(
                Instant.now().minus(Duration.ofDays(1)));
        log.info("purged {} published outbox events", removed);
    }
}