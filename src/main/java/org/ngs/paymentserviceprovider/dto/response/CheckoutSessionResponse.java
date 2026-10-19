package org.ngs.paymentserviceprovider.dto.response;

import lombok.*;
import org.ngs.paymentserviceprovider.enums.CheckoutSessionStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutSessionResponse {
    private String id;
    private String url;
    private CheckoutSessionStatus status;
    private BigDecimal amount;
    private String currency;
    private Instant expiresAt;
}
