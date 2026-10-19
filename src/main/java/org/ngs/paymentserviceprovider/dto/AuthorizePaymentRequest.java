package org.ngs.paymentserviceprovider.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizePaymentRequest {
    private String sessionUniqueId;
    private String paymentMethodUniqueId;
}