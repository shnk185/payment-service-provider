package org.ngs.paymentserviceprovider.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngs.paymentserviceprovider.dto.AuthorizePaymentRequest;
import org.ngs.paymentserviceprovider.service.AuthorizationProcessor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationListener {
    private final ObjectMapper objectMapper;
    private final AuthorizationProcessor authorizationProcessor;

    @KafkaListener(topics = "${psp.kafka.authorization-topic}", containerFactory = "kafkaListenerContainerFactory")
    public void onAuthorizationRequested(String payload) {
        AuthorizePaymentRequest request;
        try {
            request = objectMapper.readValue(payload, AuthorizePaymentRequest.class);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("malformed authorization request payload", e);
        }
        authorizationProcessor.process(request);
    }
}