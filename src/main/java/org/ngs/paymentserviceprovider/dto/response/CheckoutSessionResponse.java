package org.ngs.paymentserviceprovider.dto.response;

import lombok.*;
import org.ngs.paymentserviceprovider.enums.CheckoutSessionStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutSessionResponse {
    private String id;
    private String url;
    private CheckoutSessionStatus status;
}
