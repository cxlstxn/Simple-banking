package uk.co.asepstrath.bank;

import java.math.BigDecimal;
import java.util.UUID;

public class Account {
    private BigDecimal balance;
    private final UUID id;
    private String name;
    private boolean roundUpEnabled;
    private String postcode; 

    public Account(UUID id, String name, double balance, boolean roundUpEnabled, String postcode) { 
        this.id = id;
        this.name = name;
        this.balance = BigDecimal.valueOf(balance);
        this.roundUpEnabled = roundUpEnabled;
        this.postcode = postcode;
    }

    public Account(UUID id, String name, double balance, String postcode) {
        this(id, name, balance, false, postcode);
    }


    public void deposit(double amount) {
        balance = balance.add(BigDecimal.valueOf(amount));
    }

    public void withdraw(double amount) {
        BigDecimal amountInBigDecimal = BigDecimal.valueOf(amount);
        if (amountInBigDecimal.compareTo(balance) > 0) {
            throw new ArithmeticException("Insufficient funds");
        }
        balance = balance.subtract(amountInBigDecimal);
    }

    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return "Name: " + name + " | Balance: " + balance;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id.toString();
    }

    public void setBalance(double balance) {
        this.balance = BigDecimal.valueOf(balance);
    }

    public boolean isRoundUpEnabled() {
        return roundUpEnabled;
    }


    public String getPostcode() {
        return postcode;
    }

}

