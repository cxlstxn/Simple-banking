package uk.co.asepstrath.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertFalse;


public class AppTest {

    @BeforeEach
    public void setUp() {
        App.accounts.clear();
        App.transactions.clear();
    }

    @Test
    public void testOnStartPopulatesAccounts() {
        App app = new App();
        app.onStart();
        assertFalse(App.accounts.isEmpty(), "Accounts list should not be empty after onStart");
    }

    @Test
    public void testOnStartPopulatesTransactions() {
        App app = new App();
        app.onStart();
        assertFalse(App.transactions.isEmpty(), "Transactions list should not be empty after onStart");
    }

    @Test
    public void testOnStartPopulatesDatabaseWithAccounts() throws SQLException {
        App app = new App();
        app.onStart();
        DataSource ds = app.require(DataSource.class);
        try (Connection connection = ds.getConnection()) {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM Accounts");
            rs.next();
            int count = rs.getInt("count");
            assertFalse(count == 0, "Accounts table should not be empty after onStart");
        }
    }

    @Test
    public void testOnStartPopulatesDatabaseWithTransactions() throws SQLException {
        App app = new App();
        app.onStart();
        DataSource ds = app.require(DataSource.class);
        try (Connection connection = ds.getConnection()) {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM Transactions");
            rs.next();
            int count = rs.getInt("count");
            assertFalse(count == 0, "Transactions table should not be empty after onStart");
        }
    }
}