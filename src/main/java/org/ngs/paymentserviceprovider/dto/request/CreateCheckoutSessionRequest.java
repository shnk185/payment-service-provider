package org.ngs.paymentserviceprovider.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCheckoutSessionRequest {
    private BigDecimal amount;
    private String currency;
    private String successUrl;
    private String cancelUrl;
    private Map<String, String> metadata;
}
