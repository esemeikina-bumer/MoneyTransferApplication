package ru.netology.MoneyTransferApplication.service;

import ru.netology.MoneyTransferApplication.model.ConfirmationRequest;
import ru.netology.MoneyTransferApplication.model.TransferRequest;
import ru.netology.MoneyTransferApplication.model.TransferResponse;

public interface TransferService {
    TransferResponse transfer(TransferRequest request);
    TransferResponse confirm(ConfirmationRequest request);
}
