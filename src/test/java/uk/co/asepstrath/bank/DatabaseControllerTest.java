package uk.co.asepstrath.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import static org.mockito.Mockito.*;



public class  DatabaseControllerTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private DatabaseController databaseController;
    private Logger log;

    @BeforeEach
    public void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        log = mock(Logger.class);

        when(dataSource.getConnection()).thenReturn(connection);
        databaseController = new DatabaseController(dataSource, log);
    }

    @Test
    public void testAddAccount() throws SQLException {
        Account account = new Account(UUID.randomUUID(), "Test Account", 1000.0);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        databaseController.addAccount(account);

        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testGetBalanceFromName() throws SQLException {
        String accountName = "Test Account";
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getDouble("Balance")).thenReturn(1000.0);

        String balance = databaseController.getBalanceFromName(accountName);

        assertEquals("1000.0", balance);
    }

    @Test
    public void testGetIdFromName() throws SQLException {
        String accountName = "Test Account";
        UUID accountId = UUID.randomUUID();
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("id")).thenReturn(accountId.toString());

        UUID result = databaseController.getIdFromName(accountName);

        assertEquals(accountId, result);
    }

    @Test
    public void testEncryptPassword() {
        String password = "password123";
        String encryptedPassword = databaseController.encryptPassword(password);

        assertTrue(databaseController.verifyPassword(password, encryptedPassword));
    }

    @Test
    public void testVerifyPassword() {
        String password = "password123";
        String encryptedPassword = databaseController.encryptPassword(password);

        assertTrue(databaseController.verifyPassword(password, encryptedPassword));
        assertFalse(databaseController.verifyPassword("wrongpassword", encryptedPassword));
    }
}