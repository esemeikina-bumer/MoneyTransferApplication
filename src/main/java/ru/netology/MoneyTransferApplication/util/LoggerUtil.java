package ru.netology.MoneyTransferApplication.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerUtil {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logTransaction(String fromCard, String toCard,
                                      BigDecimal amount, BigDecimal commission, String result) {
        String logEntry = String.format(
                "[%s] From: %s To: %s Amount: %.2f Commission: %.2f Result: %s",
                LocalDateTime.now().format(DATE_FORMATTER),
                maskCardNumber(fromCard),
                maskCardNumber(toCard),
                amount,
                commission,
                result
        );
        System.out.println(logEntry);
    }

    private static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 16) {
            return "****";
        }
        return "****" + cardNumber.substring(cardNumber.length() - 4);
    }
}