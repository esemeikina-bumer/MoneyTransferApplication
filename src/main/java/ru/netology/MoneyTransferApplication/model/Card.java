package ru.netology.MoneyTransferApplication.model;

import java.math.BigDecimal;


import java.math.BigDecimal;

public class Card {
    private String cardNumber;
    private String cardHolder;
    private String cvv;
    private String expiryDate;
    private BigDecimal balance;
    private boolean active;

    // Пустой конструктор
    public Card() {
    }

    // Конструктор со всеми полями
    public Card(String cardNumber, String cardHolder, String cvv, String expiryDate,
                BigDecimal balance, boolean active) {
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
        this.cvv = cvv;
        this.expiryDate = expiryDate;
        this.balance = balance;
        this.active = active;
    }

    // Геттеры и сеттеры
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}