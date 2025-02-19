package uk.co.asepstrath.bank;

import java.math.BigDecimal;
import java.util.UUID;

public class Account {
    private BigDecimal balance;
    private final UUID id;
    private String name;
    private boolean roundUpEnabled;

    public Account(UUID id, String name, double balance, boolean roundUpEnabled) { // This is for when the api is putting in data for an account that already exists
        this.id = id;
        this.name = name;
        this.balance = BigDecimal.valueOf(balance);
        this.roundUpEnabled = roundUpEnabled;
    }

    public Account(String name) { // This is for creating a new account
        this(UUID.randomUUID(), name, 0, false);
    }

    /* This may be useful if a customer wants to create an account and already wants to put money in it
       Commented out as a customer can currently create an account and just then deposit initial amount
*/
    public Account(String name, double balance) {
        this(UUID.randomUUID(), name, balance, false);
    }


    public void deposit(double amount) {
        balance = balance.add(BigDecimal.valueOf(amount));
    }

    public void withdraw(double amount) {
        BigDecimal amountInBigDecimal = BigDecimal.valueOf(amount);
        if(amountInBigDecimal.compareTo(balance) > 0){
            throw new ArithmeticException("Insufficient funds");
        }
        balance = balance.subtract(amountInBigDecimal);
    }

    public void enableRoundUp() {
    if (!roundUpEnabled) {
        roundUpEnabled = true;
    }
    }
    public void disableRoundUp() {
        if (roundUpEnabled) {
            roundUpEnabled = false;
        }
    }
    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return "Name: " + name + " | Balance: " + balance;
        //return "ID: " + id + " | Name: " + name + " | Balance: " + balance + " | Round up: " + roundUpEnabled;
        //Not too sure what to put into the output string
    }
}
