package org.ngs.paymentserviceprovider.exception;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatus(ResponseStatusException exception) {
        String detail = exception.getReason() != null ? exception.getReason() : exception.getMessage();
        return ProblemDetail.forStatusAndDetail(exception.getStatusCode(), detail);
    }
}
