package ru.netology.MoneyTransferApplication.service;

import ru.netology.MoneyTransferApplication.model.Card;
import ru.netology.MoneyTransferApplication.model.ConfirmationRequest;
import ru.netology.MoneyTransferApplication.model.TransferRequest;
import ru.netology.MoneyTransferApplication.model.TransferResponse;
import ru.netology.MoneyTransferApplication.repository.CardRepository;
import ru.netology.MoneyTransferApplication.exception.InsufficientFundsException;
import ru.netology.MoneyTransferApplication.util.LoggerUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TransferServiceImpl implements TransferService {

    private final CardRepository cardRepository;
    private final ConcurrentHashMap<String, TransferRequest> pendingTransfers = new ConcurrentHashMap<>();
    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.01");

    @Autowired
    public TransferServiceImpl(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Override
    public TransferResponse transfer(TransferRequest request) {
        System.out.println("Processing transfer request from " +
                request.getCardFromNumber() + " to " + request.getCardToNumber());

        // Конвертируем сумму из Integer в BigDecimal
        BigDecimal amount = BigDecimal.valueOf(request.getAmount().getValue());

        // Валидация карт
        validateCards(request);

        // Проверка баланса
        Card fromCard = cardRepository.findCardByNumber(request.getCardFromNumber());
        BigDecimal commission = calculateCommission(amount);
        BigDecimal totalAmount = amount.add(commission);

        if (fromCard.getBalance().compareTo(totalAmount) < 0) {
            LoggerUtil.logTransaction(request.getCardFromNumber(),
                    request.getCardToNumber(),
                    amount,
                    commission,
                    "FAILED - Insufficient funds");
            throw new InsufficientFundsException("Insufficient funds on card. Balance: " +
                    fromCard.getBalance() + ", Required: " + totalAmount);
        }

        // Генерируем ID операции
        String operationId = UUID.randomUUID().toString();
        pendingTransfers.put(operationId, request);

        // Логируем успешную валидацию
        LoggerUtil.logTransaction(request.getCardFromNumber(),
                request.getCardToNumber(),
                amount,
                commission,
                "PENDING - Awaiting confirmation");

        return new TransferResponse(operationId);
    }

    @Override
    public TransferResponse confirm(ConfirmationRequest request) {
        System.out.println("Confirming transfer with operation ID: " + request.getOperationId());

        TransferRequest transferRequest = pendingTransfers.get(request.getOperationId());
        if (transferRequest == null) {
            throw new IllegalArgumentException("Invalid operation ID: " + request.getOperationId());
        }

        // Простая проверка кода подтверждения
        if (!"1234".equals(request.getCode())) {
            BigDecimal amount = BigDecimal.valueOf(transferRequest.getAmount().getValue());
            LoggerUtil.logTransaction(transferRequest.getCardFromNumber(),
                    transferRequest.getCardToNumber(),
                    amount,
                    calculateCommission(amount),
                    "FAILED - Invalid confirmation code");
            throw new IllegalArgumentException("Invalid confirmation code: " + request.getCode());
        }

        // Выполняем перевод
        Card fromCard = cardRepository.findCardByNumber(transferRequest.getCardFromNumber());
        Card toCard = cardRepository.findCardByNumber(transferRequest.getCardToNumber());

        BigDecimal amount = BigDecimal.valueOf(transferRequest.getAmount().getValue());
        BigDecimal commission = calculateCommission(amount);
        BigDecimal totalAmount = amount.add(commission);

        // Проверяем баланс еще раз (на случай, если изменился)
        if (fromCard.getBalance().compareTo(totalAmount) < 0) {
            pendingTransfers.remove(request.getOperationId());
            LoggerUtil.logTransaction(transferRequest.getCardFromNumber(),
                    transferRequest.getCardToNumber(),
                    amount,
                    commission,
                    "FAILED - Insufficient funds during confirmation");
            throw new InsufficientFundsException("Insufficient funds during confirmation");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(totalAmount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.updateCard(fromCard);
        cardRepository.updateCard(toCard);

        pendingTransfers.remove(request.getOperationId());

        // Логируем успешный перевод
        LoggerUtil.logTransaction(transferRequest.getCardFromNumber(),
                transferRequest.getCardToNumber(),
                amount,
                commission,
                "SUCCESS");

        return new TransferResponse(request.getOperationId());
    }

    private void validateCards(TransferRequest request) {
        boolean fromValid = cardRepository.validateCard(
                request.getCardFromNumber(),
                request.getCardFromValidTill(),
                request.getCardFromCVV()
        );

        if (!fromValid) {
            throw new IllegalArgumentException("Invalid source card details");
        }

        Card toCard = cardRepository.findCardByNumber(request.getCardToNumber());
        if (toCard == null || !toCard.isActive()) {
            throw new IllegalArgumentException("Invalid destination card: " + request.getCardToNumber());
        }

        if (request.getCardFromNumber().equals(request.getCardToNumber())) {
            throw new IllegalArgumentException("Cannot transfer to the same card");
        }
    }

    private BigDecimal calculateCommission(BigDecimal amount) {
        return amount.multiply(COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
    }
}