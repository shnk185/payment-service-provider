package org.ngs.paymentserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebhookEventPayload {
    private String id;
    private String type;
    private long created;
    private WebhookSessionData data;
}
