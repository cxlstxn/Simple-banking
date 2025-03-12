package uk.co.asepstrath.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TransactionTests {

    private Transaction transaction;

    @BeforeEach
    public void setUp() {
        // Initialize a transaction object with sample values for testing
        UUID transactionId = UUID.randomUUID();
        double amount = 100.50;
        String date = "2025-03-12";
        String from = "John Doe";
        String to = "Jane Smith";
        String type = "Transfer";

        transaction = new Transaction(transactionId, amount, date, from, to, type);
    }

    @Test
    public void testTransactionConstructor() {
        // Test that the constructor correctly sets the values
        assertNotNull(transaction.getTransactionId(), "Transaction ID should not be null");
        assertEquals(100.50, transaction.getAmount(), "Amount should be 100.50");
        assertEquals("2025-03-12", transaction.getDate(), "Date should be 2025-03-12");
        assertEquals("John Doe", transaction.getFrom(), "From should be John Doe");
        assertEquals("Jane Smith", transaction.getTo(), "To should be Jane Smith");
        assertEquals("Transfer", transaction.getType(), "Type should be Transfer");
    }

    @Test
    public void testSetTransactionId() {
        UUID newTransactionId = UUID.randomUUID();
        transaction.setTransactionId(newTransactionId);
        assertEquals(newTransactionId, transaction.getTransactionId(), "Transaction ID should be updated");
    }

    @Test
    public void testSetAmount() {
        transaction.setAmount(200.75);
        assertEquals(200.75, transaction.getAmount(), "Amount should be updated to 200.75");
    }

    @Test
    public void testSetDate() {
        transaction.setDate("2025-03-13");
        assertEquals("2025-03-13", transaction.getDate(), "Date should be updated to 2025-03-13");
    }

    @Test
    public void testSetFrom() {
        transaction.setFrom("Alice");
        assertEquals("Alice", transaction.getFrom(), "From should be updated to Alice");
    }

    @Test
    public void testSetTo() {
        transaction.setTo("Bob");
        assertEquals("Bob", transaction.getTo(), "To should be updated to Bob");
    }

    @Test
    public void testSetType() {
        transaction.setType("Deposit");
        assertEquals("Deposit", transaction.getType(), "Type should be updated to Deposit");
    }

    @Test
    public void testToString() {
        // Create the expected toString result
        String expectedString = "Transaction ID: " + transaction.getTransactionId() + " | Amount: 100.5 | Date: 2025-03-12 | From: John Doe | To: Jane Smith | Type: Transfer";
        assertEquals(expectedString, transaction.toString(), "toString should return the correct string format");
    }
}
