package org.ngs.paymentserviceprovider.dto.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutSessionResponse {
    private String id;
    private String url;
    private String status;
}
