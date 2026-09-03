package ru.netology.MoneyTransferApplication.service;

import ru.netology.MoneyTransferApplication.dto.ConfirmationRequest;
import ru.netology.MoneyTransferApplication.dto.TransferRequest;
import ru.netology.MoneyTransferApplication.dto.TransferResponse;

public interface TransferService {
    TransferResponse transfer(TransferRequest request);
    TransferResponse confirm(ConfirmationRequest request);
}
