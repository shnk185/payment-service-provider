package org.ngs.paymentserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebhookSessionData {
    private String object;
    private String id;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String lastErrorCode;
    private String lastErrorMessage;
}
