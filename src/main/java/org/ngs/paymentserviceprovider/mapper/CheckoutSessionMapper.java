package org.ngs.paymentserviceprovider.mapper;

import lombok.experimental.UtilityClass;
import org.ngs.paymentserviceprovider.dto.SessionErrorResponse;
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
                .successUrl(session.getSuccessUrl())
                .cancelUrl(session.getCancelUrl())
                .error(errorOf(session))
                .build();
    }

    private static SessionErrorResponse errorOf(CheckoutSessionEntity session) {
        if (session.getLastErrorCode() == null) {
            return null;
        }
        return SessionErrorResponse.builder()
                .code(session.getLastErrorCode())
                .message(session.getLastErrorMessage())
                .build();
    }
}