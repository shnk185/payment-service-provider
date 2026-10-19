package org.ngs.paymentserviceprovider.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngs.paymentserviceprovider.dto.request.ConfirmCheckoutSessionRequest;
import org.ngs.paymentserviceprovider.dto.request.CreateCheckoutSessionRequest;
import org.ngs.paymentserviceprovider.dto.response.CheckoutSessionResponse;
import org.ngs.paymentserviceprovider.service.CheckoutSessionService;
import org.ngs.paymentserviceprovider.utils.IdempotencyUtil;
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
        CheckoutSessionResponse response = IdempotencyUtil.executeIdempotentMethod(
                () -> checkoutSessionService.createSession(request, idempotencyKey),
                () -> checkoutSessionService.createSessionIdempotentFallback(idempotencyKey));
        log.info("create checkout session response {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CheckoutSessionResponse> getCheckoutSession(@PathVariable String id) {
        log.info("received get checkout session request {}", id);
        CheckoutSessionResponse response = checkoutSessionService.getSession(id);
        log.info("get checkout session response {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<CheckoutSessionResponse> confirmCheckoutSession(@PathVariable String id,
                                                                          @RequestBody ConfirmCheckoutSessionRequest request) {
        log.info("received confirm checkout session request {} {}", id, request);
        CheckoutSessionResponse response = checkoutSessionService.confirmSession(id, request);
        log.info("confirm checkout session response {}", response);
        return ResponseEntity.accepted().body(response);
    }
}
