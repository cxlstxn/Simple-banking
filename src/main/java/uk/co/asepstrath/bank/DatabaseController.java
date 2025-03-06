package uk.co.asepstrath.bank;

import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DatabaseController {

    private static Logger log;

    public DatabaseController(DataSource dataSource, Logger log) {
        this.dataSource = dataSource;
        this.log = log;
    }
    private final DataSource dataSource;

    public DatabaseController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setupDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            // Create necessary tables
            createTables(connection);

            // Insert accounts into the database
            insertAccounts(connection);

            // Insert transactions into the database
            insertTransactions(connection);

        } catch (SQLException e) {
            log.error("Database Creation Error", e);
        }
    }

    private void createTables(Connection connection) throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Accounts (id UUID PRIMARY KEY, Name VARCHAR(255), Balance DOUBLE)");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Transactions (id UUID PRIMARY KEY, `From` VARCHAR(255), `To` VARCHAR(255), Amount DOUBLE, Date VARCHAR(255))");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Businesses (id VARCHAR(255) PRIMARY KEY, `Name` VARCHAR(255), `Category` VARCHAR(255), `Sanctioned` VARCHAR(255))");
    }

    private void insertAccounts(Connection connection) throws SQLException {
        String insertAccountSql = "INSERT INTO Accounts (id, Name, Balance) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertAccountSql)) {
            for (Account account : App.accounts) {
                preparedStatement.setObject(1, account.getId());
                preparedStatement.setString(2, account.getName());
                preparedStatement.setDouble(3, account.getBalance().doubleValue());
                preparedStatement.executeUpdate();
            }
        }
    }

    private void insertTransactions(Connection connection) throws SQLException {
        String insertTransactionSql = "INSERT INTO Transactions (id, `From`, `To`, Amount, Date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertTransactionSql)) {
            for (Transaction transaction : App.transactions) {
                preparedStatement.setObject(1, transaction.getTransactionId());
                preparedStatement.setObject(2, transaction.getFrom());
                preparedStatement.setObject(3, transaction.getTo());
                preparedStatement.setDouble(4, transaction.getAmount());
                preparedStatement.setString(5, transaction.getDate());
                preparedStatement.executeUpdate();
            }
        }
    }

    public void addAccount(Account account) {
        String insertAccountSql = "INSERT INTO Accounts (id, Name, Balance) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertAccountSql)) {
            preparedStatement.setObject(1, UUID.randomUUID());
            preparedStatement.setString(2, account.getName());
            preparedStatement.setDouble(3, account.getBalance().doubleValue());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("Error adding account: " + account.getName(), e);
        }
    }

    public String getBalanceFromName(String accountName) {
        String query = "SELECT Balance FROM Accounts WHERE Name = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, accountName);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return String.valueOf(rs.getDouble("Balance"));
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving account with Name: " + accountName, e);
        }
        return null;
    }

    public UUID getIdFromName(String accountName) {
        String query = "SELECT id FROM Accounts WHERE Name = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, accountName);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return UUID.fromString(rs.getString("id"));
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving account with Name: " + accountName, e);
        }
        return null;
    }

    public List<Transaction> getTransactionsFromId(UUID accountId) {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT id, `From`, `To`, Amount, Date FROM Transactions WHERE `From` = ? OR `To` = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, accountId);
            preparedStatement.setObject(2, accountId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    UUID id = UUID.fromString(rs.getString("id"));
                    String from = (rs.getString("From"));
                    String to = (rs.getString("To"));
                    double amount = rs.getDouble("Amount");
                    String date = rs.getString("Date");
                    transactions.add(new Transaction(id, amount, date, from, to, "temp"));
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving transactions for account with ID: " + accountId, e);
        }
        return transactions;
    }

    public List<Transaction> getTransactionsById(UUID id) {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT id, `From`, `To`, Amount, Date FROM Transactions WHERE `From` = ? OR `To` = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, id);
            preparedStatement.setObject(2, id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    UUID transactionId = UUID.fromString(rs.getString("id"));
                    String from = (rs.getString("From"));
                    String to = (rs.getString("To"));
                    double amount = rs.getDouble("Amount");
                    String date = rs.getString("Date");
                    transactions.add(new Transaction(transactionId, amount, date, from, to, "temp"));
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving transactions for account with ID: " + id, e);
        }
        return transactions;
    }

}
