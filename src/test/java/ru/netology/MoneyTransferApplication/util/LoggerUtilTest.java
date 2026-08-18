package ru.netology.MoneyTransferApplication.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

class LoggerUtilTest {

    private static final Logger log = LoggerFactory.getLogger(LoggerUtilTest.class);

    @Test
    void logTransaction_Success() {
        // Проверяем, что метод не выбрасывает исключений
        LoggerUtil.logTransaction(
                "1111222233334444",
                "5555666677778888",
                new BigDecimal("1000.00"),
                new BigDecimal("10.00"),
                "SUCCESS"
        );
    }

    @Test
    void logTransaction_Failed() {
        LoggerUtil.logTransaction(
                "1111222233334444",
                "5555666677778888",
                new BigDecimal("1000.00"),
                new BigDecimal("10.00"),
                "FAILED - Insufficient funds"
        );
    }

    @Test
    void logTransaction_WithNullCardNumber() {
        LoggerUtil.logTransaction(
                null,
                "5555666677778888",
                new BigDecimal("1000.00"),
                new BigDecimal("10.00"),
                "SUCCESS"
        );
    }

    @Test
    void logTransaction_WithShortCardNumber() {
        LoggerUtil.logTransaction(
                "1234",
                "5555666677778888",
                new BigDecimal("1000.00"),
                new BigDecimal("10.00"),
                "SUCCESS"
        );
    }
}
