package uk.co.asepstrath.bank;

import java.math.BigDecimal;
import java.util.UUID;

public class Account {
    private BigDecimal balance;
    private final UUID id;
    private String name;
    private boolean roundUpEnabled;

    public Account(UUID id, String name, BigDecimal balance, boolean roundUpEnabled) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.roundUpEnabled = roundUpEnabled;
    }

    public void deposit(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if(amount.compareTo(balance) > 0){
            throw new ArithmeticException("Insufficient funds");
        }
        balance = balance.subtract(amount);
    }

    public BigDecimal getBalance() {
        return balance;
    }

}
