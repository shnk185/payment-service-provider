package org.ngs.paymentserviceprovider.dto.response;

import lombok.*;
import org.ngs.paymentserviceprovider.enums.PaymentMethodType;

import java.time.Instant;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodResponse {
    private String id;
    private PaymentMethodType type;
    private String brand;
    private String last4;
    private Integer expMonth;
    private Integer expYear;
    private String holder;
    private Instant createdAt;
}