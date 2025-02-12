package uk.co.asepstrath.bank;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AccountTests {

    @Test
    public void createAccount(){
        Account a = new Account();
        Assertions.assertTrue(a != null);
    }

    @Test
    public void withdraw20(){
        Account a = new Account();
        a.deposit(40);
        a.withdraw(20);
        Assertions.assertTrue(a.getBalance() == 20);
    }


}
