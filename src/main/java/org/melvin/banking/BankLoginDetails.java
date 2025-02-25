package org.melvin.banking;

public class BankLoginDetails {
    private String cardNumber;
    private String pin;
    private int balance;

    public int getBalance() {
        return balance;
    }

    public BankLoginDetails(String cardNumber, String pin) {
        this.cardNumber = cardNumber;
        this.pin = pin;
    }

    public BankLoginDetails() {
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
