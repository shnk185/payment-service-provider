package org.ngs.paymentserviceprovider.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AuthorizationErrorCode {
    CARD_DECLINED("card_declined"),
    EXPIRED_CARD("expired_card"),
    PROCESSING_ERROR("processing_error");

    private final String code;

    AuthorizationErrorCode(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }
}