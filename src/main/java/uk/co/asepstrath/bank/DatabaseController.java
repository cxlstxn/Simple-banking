package uk.co.asepstrath.bank;

import org.mindrot.jbcrypt.BCrypt;
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
import java.util.Collections;

public class DatabaseController {

    private DataSource dataSource;
    private Logger log;

    // Constructor with both DataSource and Logger
    public DatabaseController(DataSource dataSource, Logger log) {
        this.dataSource = dataSource;
        this.log = log;
    }

    // Constructor with only DataSource (optional, but ensure logger is initialized)
    public DatabaseController(DataSource dataSource) {
        this(dataSource, null); // You can provide a default logger here or throw an exception
    }

    public void setupDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            // Create necessary tables
            createTables(connection);

            // Insert accounts into the database
            insertAccounts(connection);

            // Insert businesses into the database
            insertBusinesses(connection);

            // Insert transactions into the database
            insertTransactions(connection);

        } catch (SQLException e) {
            log.error("Database Creation Error", e);
        }
    }

    private void createTables(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Accounts (id UUID PRIMARY KEY, Name VARCHAR(255), Balance DOUBLE)");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Transactions (id UUID PRIMARY KEY, `From` VARCHAR(255), `To` VARCHAR(255), Amount DOUBLE, Date VARCHAR(255))");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Businesses (id VARCHAR(255), `Name` VARCHAR(255), `Category` VARCHAR(255), `Sanctioned` VARCHAR(255))");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Users (id UUID PRIMARY KEY, `Email` VARCHAR(255), `Name` VARCHAR(255), `Password` VARCHAR(255), `Role` VARCHAR(255), `Account` UUID)");
        }
    }

    private void insertBusinesses(Connection connection) throws SQLException {
        List<List<String>> data = new ArrayList<>();
        String file = "src/main/resources/data/businesses.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                List<String> lineData = Arrays.asList(line.split(","));
                data.add(lineData);
            }
        } catch (IOException e) {
            log.error("Error reading businesses CSV file", e);
            throw new RuntimeException(e);
        }

        String insertBusinessSql = "INSERT INTO Businesses (id, Name, Category, Sanctioned) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertBusinessSql)) {
            for (List<String> list : data) {
                preparedStatement.setString(1, list.get(0));
                preparedStatement.setString(2, list.get(1));
                preparedStatement.setString(3, list.get(2));
                preparedStatement.setString(4, list.get(3));
                preparedStatement.executeUpdate();
            }
        }
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
            preparedStatement.setObject(1, account.getId());
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
            preparedStatement.setString(1, id.toString());
            preparedStatement.setObject(2, id.toString());
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

        Collections.reverse(transactions); // reversing so most recent transactions are first

        return transactions;
    }

    public void createUser(String email, String name, String password, UUID account) {
        String query = "INSERT INTO Users (id, Email, Name, Password, Role, Account) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, UUID.randomUUID());
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, name);
            preparedStatement.setString(4, password);
            preparedStatement.setString(5, "User");
            preparedStatement.setObject(6, account);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("Error creating user with Email: " + email, e);
        }
    }

    public UUID getIDfromEmail(String email) {
        String query = "SELECT Account FROM Users WHERE Email = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return UUID.fromString(rs.getString("Account"));
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving user with Email: " + email, e);
        }
        return null;
    }

    public String getNamefromID(UUID id) {
        String query = "SELECT Name FROM Users WHERE Account = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Name");
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving user with ID: " + id, e);
        }
        return null;
    }

    public String getBalanceFromID(UUID id) {
        String query = "SELECT Balance FROM Accounts WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return String.valueOf(rs.getDouble("Balance"));
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving account with ID: " + id, e);
        }
        return null;
    }

    public void transferFunds(UUID fromAccountId, UUID toAccountId, double amount) {
        String withdrawSql = "UPDATE Accounts SET Balance = Balance - ? WHERE id = ?";
        String depositSql = "UPDATE Accounts SET Balance = Balance + ? WHERE id = ?";
        String insertTransactionSql = "INSERT INTO Transactions (id, `From`, `To`, Amount, Date) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement withdrawStmt = connection.prepareStatement(withdrawSql);
                 PreparedStatement depositStmt = connection.prepareStatement(depositSql);
                 PreparedStatement transactionStmt = connection.prepareStatement(insertTransactionSql)) {

                // Withdraw from the source account
                withdrawStmt.setDouble(1, amount);
                withdrawStmt.setObject(2, fromAccountId);
                withdrawStmt.executeUpdate();

                // Deposit to the destination account
                depositStmt.setDouble(1, amount);
                depositStmt.setObject(2, toAccountId);
                depositStmt.executeUpdate();

                // Record the transaction
                transactionStmt.setObject(1, UUID.randomUUID());
                transactionStmt.setObject(2, fromAccountId);
                transactionStmt.setObject(3, toAccountId);
                transactionStmt.setDouble(4, amount);
                transactionStmt.setString(5, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date()));
                transactionStmt.executeUpdate();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error transferring funds from account " + fromAccountId + " to account " + toAccountId, e);
            }
        } catch (SQLException e) {
            log.error("Database connection error during fund transfer", e);
        }
    }

    public String encryptPassword(String unencryptedPassword){
        return BCrypt.hashpw(unencryptedPassword, BCrypt.gensalt());
    }

    public boolean verifyPassword(String enteredPassword, String encryptedPassword){
        return BCrypt.checkpw(enteredPassword, encryptedPassword);
    }

    public boolean validateUser(String email, String password) {
        String query = "SELECT Password FROM Users WHERE Email = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return verifyPassword(password, rs.getString("Password"));
                }
            }
        } catch (SQLException e) {
            log.error("Error validating user with Email: " + email, e);
        }
        return false;
    }
}