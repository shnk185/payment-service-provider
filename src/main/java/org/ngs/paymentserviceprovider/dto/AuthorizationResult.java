package org.ngs.paymentserviceprovider.dto;

import lombok.*;
import org.ngs.paymentserviceprovider.enums.AuthorizationErrorCode;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizationResult {
    private boolean approved;
    private AuthorizationErrorCode code;
    private String message;

    public static AuthorizationResult approved() {
        return AuthorizationResult.builder().approved(true).build();
    }

    public static AuthorizationResult declined(AuthorizationErrorCode code, String message) {
        return AuthorizationResult.builder().approved(false).code(code).message(message).build();
    }

    public String errorCode() {
        return code == null ? null : code.getCode();
    }
}