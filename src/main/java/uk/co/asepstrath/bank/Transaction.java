package uk.co.asepstrath.bank;

import java.util.UUID;


public class Transaction {
    private UUID transactionId;
    private double amount;
    private String date;
    private String from;
    private String to;
    private String type;

    public Transaction(UUID transactionId, double amount, String date, String from, String to, String type) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.date = date;
        this.from = from;
        this.to = to;
        this.type = type;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Transaction ID: " + transactionId + " | Amount: " + amount + " | Date: " + date + " | From: " + from + " | To: " + to + " | Type: " + type;
    }
}
