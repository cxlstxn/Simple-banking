package uk.co.asepstrath.bank;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountTests {

    @Test
    public void createAccount(){
        Account a = new Account(UUID.randomUUID(), "TestCreateAccount", new BigDecimal(0), false);
        Assertions.assertTrue(a != null);
    }

    @Test
    public void withdraw20(){
        Account a = new Account(UUID.randomUUID(), "Kaiss", new BigDecimal(0), false);
        a.deposit(40);
        a.withdraw(20);
        Assertions.assertTrue(a.getBalance() == 20);
    }


}
