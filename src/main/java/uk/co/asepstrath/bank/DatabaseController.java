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

public class DatabaseController {

    private DataSource dataSource;
    private Logger log;

    public DatabaseController(DataSource dataSource, Logger log) {
        this.dataSource = dataSource;
        this.log = log;
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
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Accounts (id VARCHAR(255), Name VARCHAR(255), Balance DOUBLE)");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Transactions (id VARCHAR(255), `From` VARCHAR(255), `To` VARCHAR(255), Amount DOUBLE, Date VARCHAR(255))");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Businesses (id VARCHAR(255), `Name` VARCHAR(255), `Category` VARCHAR(255), `Sanctioned` VARCHAR(255))");
    }

    private void insertAccounts(Connection connection) throws SQLException {
        String insertAccountSql = "INSERT INTO Accounts (id, Name, Balance) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertAccountSql)) {
            for (Account account : App.accounts) {
                preparedStatement.setString(1, account.getId().toString());
                preparedStatement.setString(2, account.getName());
                preparedStatement.setDouble(3, account.getBalance().doubleValue());
                preparedStatement.executeUpdate();
            }
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

    private void insertTransactions(Connection connection) throws SQLException {
        String insertTransactionSql = "INSERT INTO Transactions (id, `From`, `To`, Amount, Date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertTransactionSql)) {
            for (Transaction transaction : App.transactions) {
                preparedStatement.setString(1, transaction.getId());
                preparedStatement.setString(2, transaction.getFrom());
                preparedStatement.setString(3, transaction.getTo());
                preparedStatement.setDouble(4, transaction.getAmount());
                preparedStatement.setString(5, transaction.getDate());
                preparedStatement.executeUpdate();
            }
        }
    }
}
