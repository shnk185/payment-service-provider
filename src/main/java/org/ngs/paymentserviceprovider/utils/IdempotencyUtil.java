package org.ngs.paymentserviceprovider.utils;

import lombok.experimental.UtilityClass;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.function.Supplier;

@UtilityClass
public class IdempotencyUtil {

    public static <R> R executeIdempotentMethod(Supplier<R> method, Supplier<R> idempotentFallback) {
        try {
            return method.get();
        } catch (DataIntegrityViolationException e) {
            R existing = idempotentFallback.get();
            if (existing != null) {
                return existing;
            }
            throw e;
        }
    }

}
