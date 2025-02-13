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

    @Test   // Balance when a new account is created should be 0
    public void checkInitialBalance(){
        Account a = new Account(UUID.randomUUID(), "TestCheckInitialBalance", new BigDecimal(0), false);
        Assertions.assertEquals(a.getBalance(), BigDecimal.valueOf(0));
    }

    @Test   // Depositing £50 in an account with £20 should result in an account containing £70
    public void depositFunds(){
        Account a = new Account(UUID.randomUUID(), "TestDepositFunds", new BigDecimal(20), false);
        a.deposit(BigDecimal.valueOf(50));
        Assertions.assertEquals(a.getBalance(), BigDecimal.valueOf(70));
    }

    @Test   // Withdrawing £20 from an account with £40 should result in an account containing £20
    public void withdrawFunds(){
        Account a = new Account(UUID.randomUUID(), "TestWithdrawFunds", new BigDecimal(40), false);
        a.withdraw(BigDecimal.valueOf(20));
        Assertions.assertEquals(a.getBalance(), (BigDecimal.valueOf(20)));
    }

    @Test   // Withdrawing £100 from an account with £30 should throw an ArithmeticException
    public void overflowWithdrawal(){
        Account a = new Account(UUID.randomUUID(), "TestOverflowWithdrawal", new BigDecimal(30), false);
        Assertions.assertThrows(ArithmeticException.class, () -> a.withdraw(BigDecimal.valueOf(100)));
        Assertions.assertEquals(a.getBalance(), BigDecimal.valueOf(30)); // Check to see if amount changes
    }

   @Test   // Starting with an account with £20, deposit £10 five times then withdraw £20 three times. The account should end with £10
   public void depositAndWithdraw(){
       Account a = new Account(UUID.randomUUID(), "TestDepositAndWithdraw", new BigDecimal(20), false);
       for (int i = 0; i < 5; i++){
           a.deposit(BigDecimal.valueOf(10));
       }
       for (int i = 0; i < 3; i++){
           a.withdraw(BigDecimal.valueOf(20));
       }
       Assertions.assertEquals(a.getBalance(), BigDecimal.valueOf(10));
   }

   @Test    // Depositing £17.56 into an account with £5.45 should result in an account containing £23.01
    public void depositPennies(){
       Account a = new Account(UUID.randomUUID(), "TestDepositAndWithdraw", new BigDecimal("5.45"), false); // <- Late, Try find a way to not pass a string literal and have it still work
       a.deposit(BigDecimal.valueOf(17.56));
       Assertions.assertEquals(a.getBalance(), BigDecimal.valueOf(23.01));
   }
}
