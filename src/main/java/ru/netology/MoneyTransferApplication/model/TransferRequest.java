package ru.netology.MoneyTransferApplication.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TransferRequest {
    @NotBlank(message = "Card number is required")
    @JsonProperty("cardFromNumber")
    private String cardFromNumber;

    @NotBlank(message = "Card valid till is required")
    @JsonProperty("cardFromValidTill")
    private String cardFromValidTill;

    @NotBlank(message = "Card CVV is required")
    @JsonProperty("cardFromCVV")
    private String cardFromCVV;

    @NotBlank(message = "Destination card number is required")
    @JsonProperty("cardToNumber")
    private String cardToNumber;

    @NotNull(message = "Amount is required")
    @Valid
    @JsonProperty("amount")
    private Amount amount;

    public TransferRequest() {
    }

    public TransferRequest(String cardFromNumber, String cardFromValidTill, String cardFromCVV,
                           String cardToNumber, Amount amount) {
        this.cardFromNumber = cardFromNumber;
        this.cardFromValidTill = cardFromValidTill;
        this.cardFromCVV = cardFromCVV;
        this.cardToNumber = cardToNumber;
        this.amount = amount;
    }

    // Геттеры и сеттеры
    public String getCardFromNumber() {
        return cardFromNumber;
    }

    public void setCardFromNumber(String cardFromNumber) {
        this.cardFromNumber = cardFromNumber;
    }

    public String getCardFromValidTill() {
        return cardFromValidTill;
    }

    public void setCardFromValidTill(String cardFromValidTill) {
        this.cardFromValidTill = cardFromValidTill;
    }

    public String getCardFromCVV() {
        return cardFromCVV;
    }

    public void setCardFromCVV(String cardFromCVV) {
        this.cardFromCVV = cardFromCVV;
    }

    public String getCardToNumber() {
        return cardToNumber;
    }

    public void setCardToNumber(String cardToNumber) {
        this.cardToNumber = cardToNumber;
    }

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    // Внутренний класс Amount
    public static class Amount {
        @NotNull(message = "Amount value is required")
        @Positive(message = "Amount must be positive")
        @JsonProperty("value")
        private Integer value;

        @NotBlank(message = "Currency is required")
        @JsonProperty("currency")
        private String currency = "RUB";

        public Amount() {
        }

        public Amount(Integer value, String currency) {
            this.value = value;
            this.currency = currency;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }
}