package org.ngs.paymentserviceprovider.mapper;

import lombok.experimental.UtilityClass;
import org.ngs.paymentserviceprovider.dto.response.CheckoutSessionResponse;
import org.ngs.paymentserviceprovider.entity.CheckoutSessionEntity;

@UtilityClass
public class CheckoutSessionMapper {

    public static CheckoutSessionResponse dtoFromEntity(CheckoutSessionEntity session, String baseUrl) {
        return CheckoutSessionResponse.builder()
                .id(session.getUniqueId())
                .url(baseUrl + "/checkout/" + session.getUniqueId())
                .status(session.getStatus())
                .amount(session.getAmount())
                .currency(session.getCurrency())
                .expiresAt(session.getExpiresAt())
                .build();
    }
}
