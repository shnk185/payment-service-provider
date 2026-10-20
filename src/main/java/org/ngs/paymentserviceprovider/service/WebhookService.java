package org.ngs.paymentserviceprovider.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngs.paymentserviceprovider.config.properties.WebhookConfig;
import org.ngs.paymentserviceprovider.entity.CheckoutSessionEntity;
import org.ngs.paymentserviceprovider.entity.WebhookDeliveryEntity;
import org.ngs.paymentserviceprovider.enums.WebhookDeliveryStatus;
import org.ngs.paymentserviceprovider.mapper.WebhookPayloadMapper;
import org.ngs.paymentserviceprovider.repository.WebhookDeliveryRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {
    public static final String SESSION_COMPLETED = "checkout.session.completed";
    public static final String SESSION_PAYMENT_FAILED = "checkout.session.payment_failed";

    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final WebhookConfig webhookConfig;
    private final ObjectMapper objectMapper;

    @Transactional
    public void append(CheckoutSessionEntity session, String eventType) {
        if (webhookConfig.getUrl() == null || webhookConfig.getUrl().isBlank()) {
            return;
        }
        Instant now = Instant.now();
        String eventId = "evt_" + UUID.randomUUID();
        String payload = objectMapper.writeValueAsString(
                WebhookPayloadMapper.dtoFromEntity(session, eventId, eventType, now));
        webhookDeliveryRepository.save(WebhookDeliveryEntity.builder()
                .eventId(eventId)
                .eventType(eventType)
                .sessionUniqueId(session.getUniqueId())
                .targetUrl(webhookConfig.getUrl())
                .payload(payload)
                .status(WebhookDeliveryStatus.PENDING)
                .nextAttemptAt(now)
                .attempts(0)
                .build());
    }

    @Transactional
    public List<WebhookDeliveryEntity> claimBatch() {
        Instant now = Instant.now();
        List<WebhookDeliveryEntity> deliveries = webhookDeliveryRepository
                .findDueForUpdate(webhookConfig.getBatchSize(), now);
        deliveries.forEach(delivery ->
                delivery.setNextAttemptAt(now.plusSeconds(webhookConfig.getLeaseSeconds())));
        webhookDeliveryRepository.saveAll(deliveries);
        return deliveries;
    }

    @Transactional
    public void markDelivered(WebhookDeliveryEntity delivery) {
        WebhookDeliveryEntity managed = webhookDeliveryRepository.findById(delivery.getId()).orElseThrow();
        managed.setStatus(WebhookDeliveryStatus.DELIVERED);
        managed.setDeliveredAt(Instant.now());
        managed.setAttempts(managed.getAttempts() + 1);
        managed.setLastError(null);
        webhookDeliveryRepository.save(managed);
    }

    @Transactional
    public void markFailed(WebhookDeliveryEntity delivery, String error) {
        WebhookDeliveryEntity managed = webhookDeliveryRepository.findById(delivery.getId()).orElseThrow();
        int attempts = managed.getAttempts() + 1;
        managed.setAttempts(attempts);
        managed.setLastError(truncate(error));
        if (attempts >= webhookConfig.getMaxAttempts()) {
            managed.setStatus(WebhookDeliveryStatus.FAILED);
        } else {
            managed.setNextAttemptAt(Instant.now().plusSeconds(backoffSeconds(attempts)));
        }
        webhookDeliveryRepository.save(managed);
    }

    @Transactional
    public void purge() {
        long removed = webhookDeliveryRepository.deleteByStatusAndDeliveredAtBefore(
                WebhookDeliveryStatus.DELIVERED,
                Instant.now().minus(Duration.ofDays(webhookConfig.getPurgeAfterDays())));
        log.info("purged {} delivered webhooks", removed);
    }

    private long backoffSeconds(int attempts) {
        long delay = webhookConfig.getBaseBackoffSeconds() * (1L << Math.min(attempts - 1, 16));
        return Math.min(delay, webhookConfig.getMaxBackoffSeconds());
    }

    private String truncate(String error) {
        if (error == null) return null;
        return error.length() <= 1000 ? error : error.substring(0, 1000);
    }
}
