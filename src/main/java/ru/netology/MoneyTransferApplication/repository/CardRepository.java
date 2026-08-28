package ru.netology.MoneyTransferApplication.repository;

import ru.netology.MoneyTransferApplication.model.Card;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CardRepository {
    private final ConcurrentHashMap<String, Card> cards = new ConcurrentHashMap<>();

    public CardRepository() {
        // Инициализация тестовыми картами
        cards.put("1111222233334444", new Card(
                "1111222233334444",
                "John Doe",
                "123",
                "12/26",
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
                "09/24",
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
        return card.getExpiryDate().equals(validTill) &&
                card.getCvv().equals(cvv);
    }
}