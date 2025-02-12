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
    public void overflowWithdrawl(){
        Account a = new Account();
        a.deposit(30);
        Exception e = new Exception();
        Assertions.assertThrows(e, a.withdraw(100));
    }




}
