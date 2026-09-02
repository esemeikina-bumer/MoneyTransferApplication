package ru.netology.MoneyTransferApplication.repository;

import ru.netology.MoneyTransferApplication.model.Card;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CardRepository {
    private final ConcurrentHashMap<String, Card> cards = new ConcurrentHashMap<>();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/yy");

    public CardRepository() {
        // Инициализация тестовыми картами
        cards.put("1111222233334444", new Card(
                "1111222233334444",
                "John Doe",
                "123",
                "12/25",
                new BigDecimal("10000.00"),
                true
        ));
        cards.put("5555666677778888", new Card(
                "5555666677778888",
                "Jane Smith",
                "456",
                "06/26",
                new BigDecimal("5000.00"),
                true
        ));
        cards.put("1234567890123456", new Card(
                "1234567890123456",
                "Alice Johnson",
                "789",
                "09/24",  // ← просроченная карта
                new BigDecimal("7500.00"),
                true
        ));
        cards.put("9876543210987654", new Card(
                "9876543210987654",
                "Bob Wilson",
                "321",
                "03/27",
                new BigDecimal("3000.00"),
                true
        ));
    }

    public Card findCardByNumber(String cardNumber) {
        return cards.get(cardNumber);
    }

    public void updateCard(Card card) {
        cards.put(card.getCardNumber(), card);
    }

    public boolean validateCard(String cardNumber, String validTill, String cvv) {
        Card card = findCardByNumber(cardNumber);
        if (card == null || !card.isActive()) {
            return false;
        }

        // Проверка CVV
        if (!card.getCvv().equals(cvv)) {
            return false;
        }

        // Проверка срока действия
        if (!card.getExpiryDate().equals(validTill)) {
            return false;
        }

        // Проверка, что срок действия не истёк
        if (!isCardValid(card.getExpiryDate())) {
            return false;
        }

        return true;
    }

    /**
     * Проверяет, действительна ли карта по сроку действия
     * @param expiryDate дата в формате MM/yy
     * @return true, если карта действительна
     */
    private boolean isCardValid(String expiryDate) {
        try {
            YearMonth expiry = YearMonth.parse(expiryDate, DATE_FORMATTER);
            YearMonth current = YearMonth.now();
            return expiry.isAfter(current) || expiry.equals(current);
        } catch (Exception e) {
            return false;
        }
    }
}