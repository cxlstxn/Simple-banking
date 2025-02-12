package uk.co.asepstrath.bank;

import java.math.BigDecimal;
import java.util.UUID;

public class Account {
    private int balance = 0;

    private UUID id;
    private String name;
    private BigDecimal startingBalance;
    private boolean roundUpEnabled;

    public Account(UUID id, String name, BigDecimal startingBalance, boolean roundUpEnabled) {
        this.id = id;
        this.name = name;
        this.startingBalance = startingBalance;
        this.roundUpEnabled = roundUpEnabled;
    }
    public void deposit(int amount) {
        startingBalance = startingBalance.add(BigDecimal.valueOf(amount));
    }

    public int getBalance() {
        return startingBalance.intValue();
    }

}
