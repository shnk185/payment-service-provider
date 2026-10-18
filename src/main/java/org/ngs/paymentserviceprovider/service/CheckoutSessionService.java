package org.ngs.paymentserviceprovider.service;

import lombok.RequiredArgsConstructor;
import org.ngs.paymentserviceprovider.dto.request.CreateCheckoutSessionRequest;
import org.ngs.paymentserviceprovider.dto.response.CheckoutSessionResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckoutSessionService {

    public CheckoutSessionResponse createSession(CreateCheckoutSessionRequest request, String idempotencyKey) {
        throw new UnsupportedOperationException("checkout session creation not implemented yet");
    }
}
