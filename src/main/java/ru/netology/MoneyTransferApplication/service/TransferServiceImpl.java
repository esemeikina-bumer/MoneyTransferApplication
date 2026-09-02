package ru.netology.MoneyTransferApplication.service;

import ru.netology.MoneyTransferApplication.model.Card;
import ru.netology.MoneyTransferApplication.model.ConfirmationRequest;
import ru.netology.MoneyTransferApplication.model.TransferRequest;
import ru.netology.MoneyTransferApplication.model.TransferResponse;
import ru.netology.MoneyTransferApplication.repository.CardRepository;
import ru.netology.MoneyTransferApplication.exception.InsufficientFundsException;
import ru.netology.MoneyTransferApplication.util.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;
import org.springframework.beans.factory.annotation.Value;

@Service
public class TransferServiceImpl implements TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferServiceImpl.class);
    private final CardRepository cardRepository;
    private final ConcurrentHashMap<String, PendingOperation> pendingTransfers = new ConcurrentHashMap<>();
    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.01");
    private static final long EXPIRATION_MINUTES = 5; // срок жизни операции в минутах
    @Value("${app.confirmation-code}")
    private String confirmationCode;

    @Autowired
    public TransferServiceImpl(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
        // Запускаем фоновую очистку просроченных операций
        //startCleanupScheduler();
    }

    // Класс для хранения операции с временем создания
    private static class PendingOperation {
        private final TransferRequest request;
        private final LocalDateTime createdAt;

        public PendingOperation(TransferRequest request) {
            this.request = request;
            this.createdAt = LocalDateTime.now();
        }

        public TransferRequest getRequest() {
            return request;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(createdAt.plusMinutes(EXPIRATION_MINUTES));
        }
    }

    @Override
    public TransferResponse transfer(TransferRequest request) {
        log.info("Processing transfer request from {} to {}",
                maskCardNumber(request.getCardFromNumber()),
                maskCardNumber(request.getCardToNumber()));

        BigDecimal amount = BigDecimal.valueOf(request.getAmount().getValue());

        validateCards(request);

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

        String operationId = UUID.randomUUID().toString();
        pendingTransfers.put(operationId, new PendingOperation(request));

        LoggerUtil.logTransaction(request.getCardFromNumber(),
                request.getCardToNumber(),
                amount,
                commission,
                "PENDING - Awaiting confirmation");

        // Логируем размер очереди
        log.info("Pending operations count: {}", pendingTransfers.size());

        return new TransferResponse(operationId);
    }

    @Override
    public TransferResponse confirm(ConfirmationRequest request) {
        log.info("Confirming transfer with operation ID: {}", request.getOperationId());

        // Удаляем просроченные операции перед проверкой
        removeExpiredOperations();

        PendingOperation pendingOperation = pendingTransfers.get(request.getOperationId());
        if (pendingOperation == null) {
            log.warn("Invalid operation ID: {}", request.getOperationId());
            throw new IllegalArgumentException("Invalid operation ID: " + request.getOperationId());
        }

        // Проверка на просрочку (двойная проверка)
        if (pendingOperation.isExpired()) {
            pendingTransfers.remove(request.getOperationId());
            log.warn("Operation expired: {}", request.getOperationId());
            throw new IllegalArgumentException("Operation expired. Please repeat the transfer.");
        }

        TransferRequest transferRequest = pendingOperation.getRequest();

        if (!confirmationCode.equals(request.getCode())) {
            BigDecimal amount = BigDecimal.valueOf(transferRequest.getAmount().getValue());
            LoggerUtil.logTransaction(transferRequest.getCardFromNumber(),
                    transferRequest.getCardToNumber(),
                    amount,
                    calculateCommission(amount),
                    "FAILED - Invalid confirmation code");
            throw new IllegalArgumentException("Invalid confirmation code");
        }

        // Выполняем перевод
        Card fromCard = cardRepository.findCardByNumber(transferRequest.getCardFromNumber());
        Card toCard = cardRepository.findCardByNumber(transferRequest.getCardToNumber());

        BigDecimal amount = BigDecimal.valueOf(transferRequest.getAmount().getValue());
        BigDecimal commission = calculateCommission(amount);
        BigDecimal totalAmount = amount.add(commission);

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

        LoggerUtil.logTransaction(transferRequest.getCardFromNumber(),
                transferRequest.getCardToNumber(),
                amount,
                commission,
                "SUCCESS");

        return new TransferResponse(request.getOperationId());
    }

    // Метод для удаления просроченных операций
    private void removeExpiredOperations() {
        Iterator<Map.Entry<String, PendingOperation>> iterator = pendingTransfers.entrySet().iterator();
        int removedCount = 0;
        while (iterator.hasNext()) {
            Map.Entry<String, PendingOperation> entry = iterator.next();
            if (entry.getValue().isExpired()) {
                iterator.remove();
                removedCount++;
                log.debug("Removed expired operation: {}", entry.getKey());
            }
        }
        if (removedCount > 0) {
            log.info("Removed {} expired operations. Remaining: {}", removedCount, pendingTransfers.size());
        }
    }

    // Фоновый планировщик очистки (запускается каждые 30 секунд)
    private void startCleanupScheduler() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000); // 30 секунд
                    removeExpiredOperations();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
        log.info("Cleanup scheduler started for pending operations");
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

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 16) {
            return "****";
        }
        return "****" + cardNumber.substring(cardNumber.length() - 4);
    }
}