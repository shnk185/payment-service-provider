package org.ngs.paymentserviceprovider.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngs.paymentserviceprovider.dto.request.CreateCheckoutSessionRequest;
import org.ngs.paymentserviceprovider.dto.response.CheckoutSessionResponse;
import org.ngs.paymentserviceprovider.service.CheckoutSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/checkout/sessions")
@RequiredArgsConstructor
public class CheckoutSessionController {

    private final CheckoutSessionService checkoutSessionService;

    @PostMapping
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(
            @RequestBody CreateCheckoutSessionRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        log.info("received create checkout session request {}", request);
        CheckoutSessionResponse response = checkoutSessionService.createSession(request, idempotencyKey);
        log.info("create checkout session response {}", response);
        return ResponseEntity.ok(response);
    }
}
