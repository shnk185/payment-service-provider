package org.ngs.paymentserviceprovider.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "checkout_session_metadata",
        uniqueConstraints = @UniqueConstraint(columnNames = {"checkoutSessionId", "metaKey"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSessionMetadataEntity extends BaseEntity {
    @Column(nullable = false)
    private Long checkoutSessionId;

    @Column(nullable = false)
    private String metaKey;

    @Column(nullable = false)
    private String metaValue;
}
