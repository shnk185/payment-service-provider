package org.ngs.paymentserviceprovider.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngs.paymentserviceprovider.service.external.PaymentAuthorizer;
import org.ngs.paymentserviceprovider.dto.AuthorizePaymentRequest;
import org.ngs.paymentserviceprovider.dto.AuthorizationResult;
import org.ngs.paymentserviceprovider.entity.CheckoutSessionEntity;
import org.ngs.paymentserviceprovider.entity.PaymentMethodEntity;
import org.ngs.paymentserviceprovider.enums.CheckoutSessionStatus;
import org.ngs.paymentserviceprovider.repository.CheckoutSessionRepository;
import org.ngs.paymentserviceprovider.repository.PaymentMethodRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationProcessor {
    private final CheckoutSessionRepository checkoutSessionRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentAuthorizer paymentAuthorizer;
    private final WebhookService webhookService;

    @Transactional
    public void process(AuthorizePaymentRequest request) {
        CheckoutSessionEntity session = checkoutSessionRepository
                .findByUniqueIdForUpdate(request.getSessionUniqueId()).orElse(null);
        if (session == null) {
            log.warn("authorization requested for unknown session {}", request.getSessionUniqueId());
            return;
        }
        if (session.getStatus() != CheckoutSessionStatus.PROCESSING) {
            return;
        }

        PaymentMethodEntity method = paymentMethodRepository.findByUniqueId(request.getPaymentMethodUniqueId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No such payment method: " + request.getPaymentMethodUniqueId()));

        AuthorizationResult result = paymentAuthorizer.authorize(method, session.getAmount(), session.getCurrency());

        if (result.isApproved()) {
            session.setStatus(CheckoutSessionStatus.COMPLETE);
            session.setLastErrorCode(null);
            session.setLastErrorMessage(null);
        } else {
            session.setStatus(CheckoutSessionStatus.OPEN);
            session.setLastErrorCode(result.errorCode());
            session.setLastErrorMessage(result.getMessage());
        }
        checkoutSessionRepository.save(session);
        webhookService.append(session, result.isApproved()
                ? WebhookService.SESSION_COMPLETED
                : WebhookService.SESSION_PAYMENT_FAILED);
    }
}