package org.ngs.paymentserviceprovider.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.ngs.paymentserviceprovider.config.properties.PspConfig;
import org.ngs.paymentserviceprovider.dto.request.CreateCheckoutSessionRequest;
import org.ngs.paymentserviceprovider.dto.response.CheckoutSessionResponse;
import org.ngs.paymentserviceprovider.entity.CheckoutSessionEntity;
import org.ngs.paymentserviceprovider.entity.CheckoutSessionMetadataEntity;
import org.ngs.paymentserviceprovider.enums.CheckoutSessionStatus;
import org.ngs.paymentserviceprovider.mapper.CheckoutSessionMapper;
import org.ngs.paymentserviceprovider.repository.CheckoutSessionMetadataRepository;
import org.ngs.paymentserviceprovider.repository.CheckoutSessionRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckoutSessionService {
    private final CheckoutSessionRepository checkoutSessionRepository;
    private final CheckoutSessionMetadataRepository checkoutSessionMetadataRepository;
    private final PspConfig pspConfig;

    @Transactional
    public CheckoutSessionResponse createSession(CreateCheckoutSessionRequest request, String idempotencyKey) {
        CheckoutSessionResponse existingResponse = createSessionIdempotentFallback(idempotencyKey);
        if (null != existingResponse) {
            return existingResponse;
        }

        CheckoutSessionEntity session = CheckoutSessionEntity.builder().uniqueId("cs_" + UUID.randomUUID())
                .amount(request.getAmount()).currency(request.getCurrency()).status(CheckoutSessionStatus.OPEN)
                .successUrl(request.getSuccessUrl()).cancelUrl(request.getCancelUrl()).idempotencyKey(idempotencyKey)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(pspConfig.getSessionTtlMinutes()))).build();
        checkoutSessionRepository.save(session);

        Map<String, String> metadata = request.getMetadata();
        if (metadata != null) {
            List<CheckoutSessionMetadataEntity> metadataEntityList = metadata.entrySet().stream()
                    .map(x -> CheckoutSessionMetadataEntity.builder()
                            .checkoutSessionId(session.getId()).metaKey(x.getKey()).metaValue(x.getValue()).build())
                    .collect(Collectors.toList());
            checkoutSessionMetadataRepository.saveAll(metadataEntityList);
        }

        return CheckoutSessionMapper.dtoFromEntity(session, pspConfig.getBaseUrl());
    }

    public CheckoutSessionResponse createSessionIdempotentFallback(String idempotencyKey) {
        CheckoutSessionEntity existing = checkoutSessionRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existing != null) {
            return CheckoutSessionMapper.dtoFromEntity(existing, pspConfig.getBaseUrl());
        }
        return null;
    }
}
