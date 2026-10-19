package org.ngs.paymentserviceprovider.service.external;

import lombok.extern.slf4j.Slf4j;
import org.ngs.paymentserviceprovider.dto.AuthorizationResult;
import org.ngs.paymentserviceprovider.entity.PaymentMethodEntity;
import org.ngs.paymentserviceprovider.enums.AuthorizationErrorCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
public class MockPaymentAuthorizer implements PaymentAuthorizer {

    private static final Map<String, AuthorizationResult> DECLINE_RULES = Map.of(
            "4000000000000002", AuthorizationResult.declined(AuthorizationErrorCode.CARD_DECLINED,
                    "Your card was declined."),
            "4000000000000069", AuthorizationResult.declined(AuthorizationErrorCode.EXPIRED_CARD,
                    "Your card has expired."),
            "4000000000000341", AuthorizationResult.declined(AuthorizationErrorCode.PROCESSING_ERROR,
                    "An error occurred while processing your card."));

    @Override
    public AuthorizationResult authorize(PaymentMethodEntity method, BigDecimal amount, String currency) {
        log.info("authorizing {} {} for card ending {}", amount, currency, method.getLast4());
        return DECLINE_RULES.getOrDefault(method.getCardNumber(), AuthorizationResult.approved());
    }
}