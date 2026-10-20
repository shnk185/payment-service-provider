package org.ngs.paymentserviceprovider.mapper;

import lombok.experimental.UtilityClass;
import org.ngs.paymentserviceprovider.dto.WebhookEventPayload;
import org.ngs.paymentserviceprovider.dto.WebhookSessionData;
import org.ngs.paymentserviceprovider.entity.CheckoutSessionEntity;

import java.time.Instant;

@UtilityClass
public class WebhookPayloadMapper {

    public static WebhookEventPayload dtoFromEntity(CheckoutSessionEntity session, String eventId,
                                                    String eventType, Instant createdAt) {
        return WebhookEventPayload.builder()
                .id(eventId)
                .type(eventType)
                .created(createdAt.getEpochSecond())
                .data(WebhookSessionData.builder()
                        .object("checkout.session")
                        .id(session.getUniqueId())
                        .status(session.getStatus().name())
                        .amount(session.getAmount())
                        .currency(session.getCurrency())
                        .lastErrorCode(session.getLastErrorCode())
                        .lastErrorMessage(session.getLastErrorMessage())
                        .build())
                .build();
    }
}
