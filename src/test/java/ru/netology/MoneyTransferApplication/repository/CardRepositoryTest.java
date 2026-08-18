package ru.netology.MoneyTransferApplication.repository;

import ru.netology.MoneyTransferApplication.model.Card;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CardRepositoryTest {

    private CardRepository cardRepository;

    @BeforeEach
    void setUp() {
        cardRepository = new CardRepository();
    }

    @Test
    void findCardByNumber_ExistingCard_ReturnsCard() {
        Card card = cardRepository.findCardByNumber("1111222233334444");

        assertNotNull(card);
        assertEquals("1111222233334444", card.getCardNumber());
        assertEquals("John Doe", card.getCardHolder());
        assertEquals("123", card.getCvv());
        assertEquals("12/25", card.getExpiryDate());
        assertEquals(new BigDecimal("10000.00"), card.getBalance());
        assertTrue(card.isActive());
    }

    @Test
    void findCardByNumber_NonExistingCard_ReturnsNull() {
        Card card = cardRepository.findCardByNumber("9999999999999999");
        assertNull(card);
    }

    @Test
    void validateCard_ValidCard_ReturnsTrue() {
        boolean isValid = cardRepository.validateCard(
                "1111222233334444",
                "12/25",
                "123"
        );
        assertTrue(isValid);
    }

    @Test
    void validateCard_InvalidCvv_ReturnsFalse() {
        boolean isValid = cardRepository.validateCard(
                "1111222233334444",
                "12/25",
                "999"
        );
        assertFalse(isValid);
    }

    @Test
    void validateCard_InvalidExpiry_ReturnsFalse() {
        boolean isValid = cardRepository.validateCard(
                "1111222233334444",
                "01/20",
                "123"
        );
        assertFalse(isValid);
    }

    @Test
    void validateCard_NonExistingCard_ReturnsFalse() {
        boolean isValid = cardRepository.validateCard(
                "9999999999999999",
                "12/25",
                "123"
        );
        assertFalse(isValid);
    }

    @Test
    void updateCard_SuccessfullyUpdates() {
        Card card = cardRepository.findCardByNumber("1111222233334444");
        card.setBalance(new BigDecimal("5000.00"));
        card.setActive(false);

        cardRepository.updateCard(card);

        Card updatedCard = cardRepository.findCardByNumber("1111222233334444");
        assertNotNull(updatedCard);
        assertEquals(new BigDecimal("5000.00"), updatedCard.getBalance());
        assertFalse(updatedCard.isActive());
    }
}