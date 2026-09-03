package ru.netology.MoneyTransferApplication.service;

import ru.netology.MoneyTransferApplication.model.Card;
import ru.netology.MoneyTransferApplication.dto.ConfirmationRequest;
import ru.netology.MoneyTransferApplication.dto.TransferRequest;
import ru.netology.MoneyTransferApplication.dto.TransferResponse;
import ru.netology.MoneyTransferApplication.repository.CardRepository;
import ru.netology.MoneyTransferApplication.exception.InsufficientFundsException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private TransferServiceImpl transferService;

    private Card fromCard;
    private Card toCard;
    private TransferRequest validRequest;

    @BeforeEach
    void setUp() {
        fromCard = new Card(
                "1111222233334444",
                "John Doe",
                "123",
                "12/25",
                new BigDecimal("10000.00"),
                true
        );

        toCard = new Card(
                "5555666677778888",
                "Jane Smith",
                "456",
                "06/26",
                new BigDecimal("5000.00"),
                true
        );

        TransferRequest.Amount amount = new TransferRequest.Amount(1000, "RUB");
        validRequest = new TransferRequest(
                "1111222233334444",
                "12/25",
                "123",
                "5555666677778888",
                amount
        );
    }

    @Test
    void transfer_Success_ReturnsResponse() {
        when(cardRepository.validateCard(anyString(), anyString(), anyString()))
                .thenReturn(true);
        when(cardRepository.findCardByNumber("1111222233334444"))
                .thenReturn(fromCard);
        when(cardRepository.findCardByNumber("5555666677778888"))
                .thenReturn(toCard);

        TransferResponse response = transferService.transfer(validRequest);

        assertNotNull(response);
        assertNotNull(response.getOperationId());
        verify(cardRepository, times(2)).findCardByNumber(anyString());
    }

    @Test
    void transfer_InsufficientFunds_ThrowsException() {
        when(cardRepository.validateCard(anyString(), anyString(), anyString()))
                .thenReturn(true);
        when(cardRepository.findCardByNumber("1111222233334444"))
                .thenReturn(fromCard);
        when(cardRepository.findCardByNumber("5555666677778888"))
                .thenReturn(toCard);

        validRequest.getAmount().setValue(1000000);

        assertThrows(InsufficientFundsException.class,
                () -> transferService.transfer(validRequest));
    }

    @Test
    void transfer_InvalidCard_ThrowsException() {
        when(cardRepository.validateCard(anyString(), anyString(), anyString()))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> transferService.transfer(validRequest));
    }

    @Test
    void confirm_Success_ReturnsResponse() {
        when(cardRepository.validateCard(anyString(), anyString(), anyString()))
                .thenReturn(true);
        when(cardRepository.findCardByNumber("1111222233334444"))
                .thenReturn(fromCard);
        when(cardRepository.findCardByNumber("5555666677778888"))
                .thenReturn(toCard);

        TransferResponse transferResponse = transferService.transfer(validRequest);

        ConfirmationRequest confirmation = new ConfirmationRequest(
                transferResponse.getOperationId(),
                "1234"
        );

        when(cardRepository.findCardByNumber("1111222233334444"))
                .thenReturn(fromCard);
        when(cardRepository.findCardByNumber("5555666677778888"))
                .thenReturn(toCard);

        TransferResponse confirmResponse = transferService.confirm(confirmation);

        assertNotNull(confirmResponse);
        assertEquals(transferResponse.getOperationId(), confirmResponse.getOperationId());
        verify(cardRepository, times(2)).updateCard(any(Card.class));
    }

    @Test
    void confirm_InvalidCode_ThrowsException() {
        when(cardRepository.validateCard(anyString(), anyString(), anyString()))
                .thenReturn(true);
        when(cardRepository.findCardByNumber("1111222233334444"))
                .thenReturn(fromCard);
        when(cardRepository.findCardByNumber("5555666677778888"))
                .thenReturn(toCard);

        TransferResponse transferResponse = transferService.transfer(validRequest);

        ConfirmationRequest confirmation = new ConfirmationRequest(
                transferResponse.getOperationId(),
                "9999"
        );

        assertThrows(IllegalArgumentException.class,
                () -> transferService.confirm(confirmation));

        verify(cardRepository, never()).updateCard(any(Card.class));
    }

    @Test
    void confirm_InvalidOperationId_ThrowsException() {
        ConfirmationRequest confirmation = new ConfirmationRequest(
                "invalid-id",
                "1234"
        );

        assertThrows(IllegalArgumentException.class,
                () -> transferService.confirm(confirmation));
    }
}