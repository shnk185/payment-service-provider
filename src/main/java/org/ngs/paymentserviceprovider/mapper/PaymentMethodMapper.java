package org.ngs.paymentserviceprovider.mapper;

import lombok.experimental.UtilityClass;
import org.ngs.paymentserviceprovider.dto.response.PaymentMethodResponse;
import org.ngs.paymentserviceprovider.entity.PaymentMethodEntity;

@UtilityClass
public class PaymentMethodMapper {

    public static PaymentMethodResponse dtoFromEntity(PaymentMethodEntity entity) {
        return PaymentMethodResponse.builder()
                .id(entity.getUniqueId())
                .type(entity.getType())
                .brand(entity.getBrand())
                .last4(entity.getLast4())
                .expMonth(entity.getExpMonth())
                .expYear(entity.getExpYear())
                .holder(entity.getHolder())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}