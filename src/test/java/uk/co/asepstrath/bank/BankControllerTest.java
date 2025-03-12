package uk.co.asepstrath.bank;

import io.jooby.MockRouter;
import io.jooby.ModelAndView;
import io.jooby.StatusCode;
import io.jooby.exception.StatusCodeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class BankControllerTest {

    @Mock
    private DataSource mockDataSource;

    @Mock
    private Logger mockLogger;

    @Mock
    private Connection mockConnection;

    @Mock
    private Statement mockStatement;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private DatabaseController mockDatabaseController;

    private BankController bankController;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        bankController = new BankController(mockDataSource, mockLogger);

        // Initialize App.accounts and App.transactions for testing
        App.accounts = new java.util.ArrayList<>();
        App.transactions = new java.util.ArrayList<>();

        // Add test accounts and transactions
        UUID testId = UUID.randomUUID();
        App.accounts.add(new Account(testId, "Test User", 1000.0));

        Transaction testTransaction = new Transaction();
        // Set transaction properties as needed
        App.transactions.add(testTransaction);
    }

    @Test
    public void testWelcome() {
        String result = bankController.welcome();
        assertEquals("Welcome to Jooby!", result);
    }

    @Test
    public void testListAccounts() {
        String result = bankController.listAccounts();
        assertTrue(result.contains("Account List"));
        assertTrue(result.contains("Test User"));
    }

    @Test
    public void testListTransactions() {
        String result = bankController.listTransactions();
        assertTrue(result.contains("Transaction List"));
    }

    @Test
    public void testLoginWithoutEmail() {
        ModelAndView modelAndView = bankController.login(null);

        assertEquals("login.hbs", modelAndView.getView());
        Map<String, Object> model = modelAndView.getModel();
        assertEquals("Your", model.get("email"));
    }

    @Test
    public void testLoginWithEmail() {
        ModelAndView modelAndView = bankController.login("test@example.com");

        assertEquals("login.hbs", modelAndView.getView());
        Map<String, Object> model = modelAndView.getModel();
        assertEquals("test@example.com's", model.get("email"));
    }

    @Test
    public void testDashboard() {
        // Create a spy on the controller to mock the database calls
        BankController spyController = spy(bankController);

        // Setup mock DatabaseController
        UUID mockId = UUID.randomUUID();
        DatabaseController mockDbController = mock(DatabaseController.class);

        // Mock the creation of DatabaseController
        doReturn(mockDbController).when(spyController).new DatabaseController(any(DataSource.class));

        when(mockDbController.getIDfromEmail("test@example.com")).thenReturn(mockId);
        when(mockDbController.getNamefromID(mockId)).thenReturn("Test User");
        when(mockDbController.getBalanceFromID(mockId)).thenReturn("1000.0");
        when(mockDbController.getTransactionsById(mockId)).thenReturn(new java.util.ArrayList<>());

        ModelAndView modelAndView = spyController.dashboard("test@example.com");

        assertEquals("dashboard.hbs", modelAndView.getView());
        Map<String, Object> model = modelAndView.getModel();
        assertEquals("test@example.com", model.get("email"));
        assertEquals("Test User", model.get("name"));
        assertEquals("1000.0", model.get("balance"));
        assertEquals(mockId, model.get("id"));
    }

    @Test
    public void testTransfer() {
        // Create a spy on the controller to mock the database calls
        BankController spyController = spy(bankController);

        // Setup mock DatabaseController
        UUID mockId = UUID.randomUUID();
        DatabaseController mockDbController = mock(DatabaseController.class);

        // Mock the creation of DatabaseController
        doReturn(mockDbController).when(spyController).new DatabaseController(any(DataSource.class));

        when(mockDbController.getIDfromEmail("test@example.com")).thenReturn(mockId);
        when(mockDbController.getNamefromID(mockId)).thenReturn("Test User");
        when(mockDbController.getBalanceFromID(mockId)).thenReturn("1000.0");

        ModelAndView modelAndView = spyController.transfer("test@example.com");

        assertEquals("transfer.hbs", modelAndView.getView());
        Map<String, Object> model = modelAndView.getModel();
        assertEquals("Test User", model.get("name"));
        assertEquals("1000.0", model.get("balance"));
        assertEquals(mockId, model.get("id"));
        assertEquals("test@example.com", model.get("email"));
    }

    @Test
    public void testHandleTransferSuccess() {
        // Create a spy on the controller to mock the database calls
        BankController spyController = spy(bankController);

        // Setup mock DatabaseController
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        DatabaseController mockDbController = mock(DatabaseController.class);

        // Mock the creation of DatabaseController
        doReturn(mockDbController).when(spyController).new DatabaseController(any(DataSource.class));

        when(mockDbController.getIDfromEmail("from@example.com")).thenReturn(fromId);
        when(mockDbController.getNamefromID(fromId)).thenReturn("From User");
        when(mockDbController.getBalanceFromID(fromId)).thenReturn("1000.0");
        when(mockDbController.getTransactionsById(fromId)).thenReturn(new java.util.ArrayList<>());

        ModelAndView modelAndView = spyController.handleTransfer("from@example.com", toId.toString(), 500.0);

        // Verify transfer was made
        verify(mockDbController).transferFunds(fromId, toId, 500.0);

        assertEquals("dashboard.hbs", modelAndView.getView());
        Map<String, Object> model = modelAndView.getModel();
        assertEquals("from@example.com", model.get("email"));
        assertEquals("From User", model.get("name"));
        assertEquals("1000.0", model.get("balance"));
        assertEquals(fromId, model.get("id"));
    }

    @Test
    public void testHandleTransferInvalidDetails() {
        Exception exception = assertThrows(StatusCodeException.class, () -> {
            bankController.handleTransfer(null, UUID.randomUUID().toString(), 100.0);
        });

        assertTrue(exception.getMessage().contains("Invalid transfer details"));
    }

    @Test
    public void testHandleTransferNegativeAmount() {
        Exception exception = assertThrows(StatusCodeException.class, () -> {
            bankController.handleTransfer("from@example.com", UUID.randomUUID().toString(), -100.0);
        });

        assertTrue(exception.getMessage().contains("Invalid transfer details"));
    }

    @Test
    public void testHandleTransferToSameAccount() {
        // Create a spy on the controller to mock the database calls
        BankController spyController = spy(bankController);

        // Setup mock DatabaseController
        UUID accountId = UUID.randomUUID();
        DatabaseController mockDbController = mock(DatabaseController.class);

        // Mock the creation of DatabaseController
        doReturn(mockDbController).when(spyController).new DatabaseController(any(DataSource.class));

        when(mockDbController.getIDfromEmail("user@example.com")).thenReturn(accountId);

        Exception exception = assertThrows(StatusCodeException.class, () -> {
            spyController.handleTransfer("user@example.com", accountId.toString(), 100.0);
        });

        assertTrue(exception.getMessage().contains("Cannot transfer to the same account"));
    }

    @Test
    public void testHandleTransferInsufficientFunds() {
        // Create a spy on the controller to mock the database calls
        BankController spyController = spy(bankController);

        // Setup mock DatabaseController
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        DatabaseController mockDbController = mock(DatabaseController.class);

        // Mock the creation of DatabaseController
        doReturn(mockDbController).when(spyController).new DatabaseController(any(DataSource.class));

        when(mockDbController.getIDfromEmail("from@example.com")).thenReturn(fromId);
        when(mockDbController.getBalanceFromID(fromId)).thenReturn("500.0");

        Exception exception = assertThrows(StatusCodeException.class, () -> {
            spyController.handleTransfer("from@example.com", toId.toString(), 1000.0);
        });

        assertTrue(exception.getMessage().contains("Insufficient funds"));
    }

    @Test
    public void testSignup() {
        ModelAndView modelAndView = bankController.signup();

        assertEquals("signup.hbs", modelAndView.getView());
    }

    @Test
    public void testHandleSignupSuccess() {
        // Create a spy on the controller to mock the database calls
        BankController spyController = spy(bankController);

        // Setup mock DatabaseController
        DatabaseController mockDbController = mock(DatabaseController.class);

        // Mock the creation of DatabaseController
        doReturn(mockDbController).when(spyController).new DatabaseController(any(DataSource.class), any(Logger.class));

        ModelAndView modelAndView = spyController.handleSignup("new@example.com", "New User", "password123");

        // Verify user was created
        verify(mockDbController).createUser(eq("new@example.com"), eq("New User"), eq("password123"), any(UUID.class));
        verify(mockDbController).addAccount(any(Account.class));

        assertEquals("login.hbs", modelAndView.getView());
    }

    @Test
    public void testHandleSignupMissingDetails() {
        Exception exception = assertThrows(StatusCodeException.class, () -> {
            bankController.handleSignup("", "New User", "password123");
        });

        assertTrue(exception.getMessage().contains("Name and password are required"));

        exception = assertThrows(StatusCodeException.class, () -> {
            bankController.handleSignup("new@example.com", "", "password123");
        });

        assertTrue(exception.getMessage().contains("Name and password are required"));

        exception = assertThrows(StatusCodeException.class, () -> {
            bankController.handleSignup("new@example.com", "New User", "");
        });

        assertTrue(exception.getMessage().contains("Name and password are required"));
    }
}