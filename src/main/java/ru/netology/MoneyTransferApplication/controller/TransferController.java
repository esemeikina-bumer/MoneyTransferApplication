package ru.netology.MoneyTransferApplication.controller;

import ru.netology.MoneyTransferApplication.model.ConfirmationRequest;
import ru.netology.MoneyTransferApplication.model.TransferRequest;
import ru.netology.MoneyTransferApplication.model.TransferResponse;
import ru.netology.MoneyTransferApplication.service.TransferService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
@Tag(name = "Money Transfer API", description = "API for money transfers between cards")
public class TransferController {

    private static final Logger log = LoggerFactory.getLogger(TransferController.class);
    private final TransferService transferService;

    @Autowired
    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 16) {
            return "****";
        }
        return "****" + cardNumber.substring(cardNumber.length() - 4);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money card to card",
            description = "Call to send money between cards")
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        log.info("Received transfer request: from {} to {}",
                maskCardNumber(request.getCardFromNumber()),
                maskCardNumber(request.getCardToNumber()));
        TransferResponse response = transferService.transfer(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/confirmOperation")
    @Operation(summary = "Confirm operation",
            description = "Confirming operation with code")
    public ResponseEntity<TransferResponse> confirm(@Valid @RequestBody ConfirmationRequest request) {
        log.info("Received confirmation request for operation: {}", request.getOperationId());
        TransferResponse response = transferService.confirm(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the service is running")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Service is running");
    }
}