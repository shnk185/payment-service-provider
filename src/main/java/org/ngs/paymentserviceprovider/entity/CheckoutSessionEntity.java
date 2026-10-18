package org.ngs.paymentserviceprovider.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ngs.paymentserviceprovider.enums.CheckoutSessionStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "checkout_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSessionEntity extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String uniqueId;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CheckoutSessionStatus status;

    private String successUrl;
    private String cancelUrl;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
