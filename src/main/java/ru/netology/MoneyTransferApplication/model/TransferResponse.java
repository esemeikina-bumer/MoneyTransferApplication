package ru.netology.MoneyTransferApplication.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransferResponse {
    @JsonProperty("operationId")
    private String operationId;

    public TransferResponse() {
    }

    public TransferResponse(String operationId) {
        this.operationId = operationId;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }
}