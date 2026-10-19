package org.ngs.paymentserviceprovider.dto.request;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmCheckoutSessionRequest {
    private String paymentMethodId;
}