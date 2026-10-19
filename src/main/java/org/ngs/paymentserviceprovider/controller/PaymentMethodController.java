package org.ngs.paymentserviceprovider.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngs.paymentserviceprovider.dto.request.CreatePaymentMethodRequest;
import org.ngs.paymentserviceprovider.dto.response.PaymentMethodResponse;
import org.ngs.paymentserviceprovider.service.PaymentMethodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/v1/payment_methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @PostMapping
    public ResponseEntity<PaymentMethodResponse> createPaymentMethod(@RequestBody CreatePaymentMethodRequest request) {
        log.info("received create payment method request type={} holder={}",
                request.getType(), request.getCard() != null ? request.getCard().getHolder() : null);
        PaymentMethodResponse response = paymentMethodService.createPaymentMethod(request);
        log.info("create payment method response {}", response);
        return ResponseEntity.ok(response);
    }
}