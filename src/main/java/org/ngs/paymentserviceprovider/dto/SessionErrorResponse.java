package org.ngs.paymentserviceprovider.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionErrorResponse {
    private String code;
    private String message;
}