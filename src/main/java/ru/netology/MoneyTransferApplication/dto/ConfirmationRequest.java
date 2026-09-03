package ru.netology.MoneyTransferApplication.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class ConfirmationRequest {
    @NotBlank(message = "Operation ID is required")
    @JsonProperty("operationId")
    private String operationId;

    @NotBlank(message = "Confirmation code is required")
    @JsonProperty("code")
    private String code;

    public ConfirmationRequest() {
    }

    public ConfirmationRequest(String operationId, String code) {
        this.operationId = operationId;
        this.code = code;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}