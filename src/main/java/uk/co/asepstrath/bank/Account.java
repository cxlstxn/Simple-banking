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

    public void deposit(int amount) {
        balance = balance.add(BigDecimal.valueOf(amount));
    }

    public void withdraw(int amount) {
        balance = balance.subtract(BigDecimal.valueOf(amount));
    }

    public int getBalance() {
        return balance.intValue();
    }

}
