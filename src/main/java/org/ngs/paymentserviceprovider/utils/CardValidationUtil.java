package org.ngs.paymentserviceprovider.utils;

import lombok.experimental.UtilityClass;
import org.ngs.paymentserviceprovider.exception.BadRequestException;

import java.time.YearMonth;

@UtilityClass
public class CardValidationUtil {

    public static String normaliseNumber(String raw) {
        if (raw == null) {
            throw new BadRequestException( "Card number is required");
        }
        return raw.replaceAll("[\\s-]", "");
    }

    public static void validateNumber(String number) {
        if (!number.matches("\\d{12,19}") || !passesLuhn(number)) {
            throw new BadRequestException("Card number is invalid");
        }
    }

    public static void validateExpiry(Integer expMonth, Integer expYear) {
        if (expMonth == null || expMonth < 1 || expMonth > 12 || expYear == null) {
            throw new BadRequestException("Card expiry is invalid");
        }
        if (YearMonth.of(expYear, expMonth).isBefore(YearMonth.now())) {
            throw new BadRequestException("Card has expired");
        }
    }

    public static String brandOf(String number) {
        if (number.startsWith("4")) {
            return "visa";
        }
        if (number.startsWith("51") || number.startsWith("52") || number.startsWith("53")
                || number.startsWith("54") || number.startsWith("55")) {
            return "mastercard";
        }
        if (number.startsWith("34") || number.startsWith("37")) {
            return "amex";
        }
        if (number.startsWith("6011") || number.startsWith("65")) {
            return "discover";
        }
        return "unknown";
    }

    private static boolean passesLuhn(String number) {
        int sum = 0;
        boolean doubleDigit = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }
}