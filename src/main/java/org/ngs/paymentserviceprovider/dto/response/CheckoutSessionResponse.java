package org.ngs.paymentserviceprovider.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.ngs.paymentserviceprovider.dto.SessionErrorResponse;
import org.ngs.paymentserviceprovider.enums.CheckoutSessionStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckoutSessionResponse {
    private String id;
    private String url;
    private CheckoutSessionStatus status;
    private BigDecimal amount;
    private String currency;
    private Instant expiresAt;
    private String successUrl;
    private String cancelUrl;
    private SessionErrorResponse error;
}