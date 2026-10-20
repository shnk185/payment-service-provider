package org.ngs.paymentserviceprovider.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ngs.paymentserviceprovider.enums.WebhookDeliveryStatus;

import java.time.Instant;

@Entity
@Table(name = "webhook_deliveries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookDeliveryEntity extends BaseEntity {
    @Column(name = "event_id", nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "session_unique_id", nullable = false, length = 64)
    private String sessionUniqueId;

    @Column(name = "target_url", nullable = false, length = 512)
    private String targetUrl;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WebhookDeliveryStatus status;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;
}
