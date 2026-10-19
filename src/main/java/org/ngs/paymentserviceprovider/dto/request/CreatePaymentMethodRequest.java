package org.ngs.paymentserviceprovider.dto.request;

import lombok.*;
import org.ngs.paymentserviceprovider.dto.CardDetails;
import org.ngs.paymentserviceprovider.enums.PaymentMethodType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentMethodRequest {
    private PaymentMethodType type;
    private CardDetails card;
}