package uk.co.asepstrath.bank;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountTests {

    @Test
    public void createAccount(){
        Account a = new Account(UUID.randomUUID(), "TestCreateAccount", 0, false);
        Account b = new Account(null, "TestCreateAccount", 0);
        Assertions.assertNotNull(a);
        Assertions.assertNotNull(b);
    }

    @Test   // Balance when a new account is created should be 0
    public void checkInitialBalance(){
        Account a = new Account(null, "TestCheckInitialBalance", 0); // when a new account is created then the name only needs to be inputted
        Assertions.assertEquals(BigDecimal.valueOf(0.0), a.getBalance());
    }

    @Test   // Depositing £50 in an account with £20 should result in an account containing £70
    public void depositFunds(){
        Account a = new Account(UUID.randomUUID(), "TestDepositFunds", 20, false);
        a.deposit(50);
        Assertions.assertEquals(BigDecimal.valueOf(70.0), a.getBalance());
    }

    @Test   // Withdrawing £20 from an account with £40 should result in an account containing £20
    public void withdrawFunds(){
        Account a = new Account(UUID.randomUUID(), "TestWithdrawFunds", 40, false);
        a.withdraw(20);
        Assertions.assertEquals(BigDecimal.valueOf(20.0), a.getBalance());
    }

    @Test   // Withdrawing £100 from an account with £30 should throw an ArithmeticException
    public void overflowWithdrawal(){
        Account a = new Account(UUID.randomUUID(), "TestOverflowWithdrawal", 30, false);
        Assertions.assertThrows(ArithmeticException.class, () -> a.withdraw(100));
        Assertions.assertEquals(BigDecimal.valueOf(30.0), a.getBalance()); // Check to see if amount changes
    }

    @Test   // Starting with an account with £20, deposit £10 five times then withdraw £20 three times. The account should end with £10
    public void depositAndWithdraw(){
        Account a = new Account(UUID.randomUUID(), "TestDepositAndWithdraw", 20, false);
        for (int i = 0; i < 5; i++){
            a.deposit(10);
        }
        for (int i = 0; i < 3; i++){
            a.withdraw(20);
        }
        Assertions.assertEquals(BigDecimal.valueOf(10.0), a.getBalance());
    }

    @Test    // Depositing £17.56 into an account with £5.45 should result in an account containing £23.01
    public void depositPennies(){
        Account a = new Account(UUID.randomUUID(), "TestDepositAndWithdraw", 5.45, false);
        a.deposit(17.56);
        Assertions.assertEquals(BigDecimal.valueOf(23.01), a.getBalance());
    }

    @Test
    public void stringAccount(){  //Testing override of .toString() method to return a string with the account name and balance
        Account a = new Account(UUID.randomUUID(), "TestStringAccount", 10, false);
        Assertions.assertEquals("Name: TestStringAccount | Balance: 10.0", a.toString());
    }
}

