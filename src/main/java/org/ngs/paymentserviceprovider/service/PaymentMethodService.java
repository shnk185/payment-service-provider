package org.ngs.paymentserviceprovider.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.ngs.paymentserviceprovider.dto.CardDetails;
import org.ngs.paymentserviceprovider.dto.request.CreatePaymentMethodRequest;
import org.ngs.paymentserviceprovider.dto.response.PaymentMethodResponse;
import org.ngs.paymentserviceprovider.entity.PaymentMethodEntity;
import org.ngs.paymentserviceprovider.enums.PaymentMethodType;
import org.ngs.paymentserviceprovider.mapper.PaymentMethodMapper;
import org.ngs.paymentserviceprovider.repository.PaymentMethodRepository;
import org.ngs.paymentserviceprovider.utils.CardValidationUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {
    private final PaymentMethodRepository paymentMethodRepository;

    @Transactional
    public PaymentMethodResponse createPaymentMethod(CreatePaymentMethodRequest request) {
        CardDetails card = request.getType() == PaymentMethodType.CARD ? request.getCard() : null;
        if (card == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only card payment methods are supported");
        }

        String number = CardValidationUtil.normaliseNumber(card.getNumber());
        CardValidationUtil.validateNumber(number);
        CardValidationUtil.validateExpiry(card.getExpMonth(), card.getExpYear());

        PaymentMethodEntity entity = PaymentMethodEntity.builder().uniqueId("pm_" + UUID.randomUUID())
                .type(PaymentMethodType.CARD).cardNumber(number).brand(CardValidationUtil.brandOf(number))
                .last4(number.substring(number.length() - 4)).expMonth(card.getExpMonth())
                .expYear(card.getExpYear()).holder(card.getHolder()).build();
        paymentMethodRepository.save(entity);

        return PaymentMethodMapper.dtoFromEntity(entity);
    }
}