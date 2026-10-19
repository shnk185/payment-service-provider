package org.ngs.paymentserviceprovider.service.external;

import org.ngs.paymentserviceprovider.dto.AuthorizationResult;
import org.ngs.paymentserviceprovider.entity.PaymentMethodEntity;

import java.math.BigDecimal;

public interface PaymentAuthorizer {
    AuthorizationResult authorize(PaymentMethodEntity method, BigDecimal amount, String currency);
}