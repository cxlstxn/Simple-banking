package uk.co.asepstrath.bank;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AccountTests {

    @Test
    public void createAccount(){
        Account a = new Account();
        Assertions.assertNotNull(a);
        Assertions.assertEquals(0, a.getBalance()); // Ensure initial balance is 0
    }

    @Test
    public void depositFunds() {
        Account a = new Account();
        a.deposit(20); // Deposit £20
        a.deposit(50); // Deposit another £50
        Assertions.assertEquals(70, a.getBalance()); // Ensure balance is £70
    }



}
