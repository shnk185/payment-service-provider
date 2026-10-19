package org.ngs.paymentserviceprovider.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardDetails {
    private String number;
    private Integer expMonth;
    private Integer expYear;
    private String cvc;
    private String holder;
}