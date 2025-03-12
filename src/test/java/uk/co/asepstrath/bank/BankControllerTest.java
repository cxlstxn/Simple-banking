package uk.co.asepstrath.bank;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.sql.DataSource;

public class BankControllerTest {

    @Mock
    private DataSource mockDataSource;

    @Mock
    private Logger mockLogger;

    @Mock
    private DatabaseController mockDbController;

    private BankController bankController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        bankController = new BankController(mockDataSource, mockLogger);
    }

    @Test
    public void testLogin() {
        // Arrange
        String testEmail = "test@example.com";

        // Act
        ModelAndView result = bankController.login(testEmail);

        // Assert
        assertEquals("login.hbs", result.getTemplate());
        Map<String, Object> model = result.getModel();
        assertEquals(testEmail, model.get("email"));
    }

    @Test
    public void testDashboard() {
        // Arrange
        String testEmail = "user@example.com";
        UUID testId = UUID.randomUUID();
        String testName = "Test User";
        String testBalance = "1000.00";
        List<Transaction> testTransactions = Arrays.asList(new Transaction("Test transaction", 100.0));

        // Create a spy for DatabaseController to avoid actual database calls
        try (MockedStatic<DatabaseController> mockedDbController = mockStatic(DatabaseController.class)) {
            mockedDbController.when(() -> new DatabaseController(mockDataSource)).thenReturn(mockDbController);

            when(mockDbController.getIDfromEmail(testEmail)).thenReturn(testId);
            when(mockDbController.getNamefromID(testId)).thenReturn(testName);
            when(mockDbController.getBalanceFromID(testId)).thenReturn(testBalance);
            when(mockDbController.getTransactionsById(testId)).thenReturn(testTransactions);

            // Act
            ModelAndView result = bankController.dashboard(testEmail);

            // Assert
            assertEquals("dashboard.hbs", result.getTemplate());
            Map<String, Object> model = result.getModel();
            assertEquals(testEmail, model.get("email"));
            assertEquals(testName, model.get("name"));
            assertEquals(testBalance, model.get("balance"));
            assertEquals(testId, model.get("id"));
            assertEquals(testTransactions, model.get("transactions"));
        }
    }

    @Test
    public void testTransfer() {
        // Arrange
        String testEmail = "user@example.com";
        UUID testId = UUID.randomUUID();
        String testName = "Test User";
        String testBalance = "1000.00";

        // Create a spy for DatabaseController to avoid actual database calls
        try (MockedStatic<DatabaseController> mockedDbController = mockStatic(DatabaseController.class)) {
            mockedDbController.when(() -> new DatabaseController(mockDataSource)).thenReturn(mockDbController);

            when(mockDbController.getIDfromEmail(testEmail)).thenReturn(testId);
            when(mockDbController.getNamefromID(testId)).thenReturn(testName);
            when(mockDbController.getBalanceFromID(testId)).thenReturn(testBalance);

            // Act
            ModelAndView result = bankController.transfer(testEmail);

            // Assert
            assertEquals("transfer.hbs", result.getTemplate());
            Map<String, Object> model = result.getModel();
            assertEquals(testName, model.get("name"));
            assertEquals(testBalance, model.get("balance"));
            assertEquals(testId, model.get("id"));
            assertEquals(testEmail, model.get("email"));
        }
    }

    @Test
    public void testHandleTransfer_Success() {
        // Arrange
        String fromEmail = "from@example.com";
        String toIdString = UUID.randomUUID().toString();
        double amount = 100.0;

        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.fromString(toIdString);
        String fromName = "From User";
        String fromBalance = "1000.00";
        List<Transaction> transactions = Arrays.asList(new Transaction("Test transaction", 100.0));

        // Create a spy for DatabaseController to avoid actual database calls
        try (MockedStatic<DatabaseController> mockedDbController = mockStatic(DatabaseController.class)) {
            mockedDbController.when(() -> new DatabaseController(mockDataSource)).thenReturn(mockDbController);

            when(mockDbController.getIDfromEmail(fromEmail)).thenReturn(fromId);
            when(mockDbController.getBalanceFromID(fromId)).thenReturn("1000.00");
            when(mockDbController.getNamefromID(fromId)).thenReturn(fromName);
            when(mockDbController.getTransactionsById(fromId)).thenReturn(transactions);

            // Act
            ModelAndView result = bankController.handleTransfer(fromEmail, toIdString, amount);

            // Assert
            verify(mockDbController).transferFunds(fromId, toId, amount);
            assertEquals("dashboard.hbs", result.getTemplate());
            Map<String, Object> model = result.getModel();
            assertEquals(fromEmail, model.get("email"));
            assertEquals(fromName, model.get("name"));
            assertEquals(fromBalance, model.get("balance"));
            assertEquals(fromId, model.get("id"));
            assertEquals(transactions, model.get("transactions"));
        }
    }

    @Test
    public void testHandleTransfer_InvalidParams() {
        // Test with null email
        StatusCodeException exception = assertThrows(StatusCodeException.class, () ->
                bankController.handleTransfer(null, UUID.randomUUID().toString(), 100.0)
        );
        assertEquals(StatusCode.BAD_REQUEST, exception.getStatusCode());

        // Test with null destination
        exception = assertThrows(StatusCodeException.class, () ->
                bankController.handleTransfer("email@example.com", null, 100.0)
        );
        assertEquals(StatusCode.BAD_REQUEST, exception.getStatusCode());

        // Test with zero amount
        exception = assertThrows(StatusCodeException.class, () ->
                bankController.handleTransfer("email@example.com", UUID.randomUUID().toString(), 0)
        );
        assertEquals(StatusCode.BAD_REQUEST, exception.getStatusCode());

        // Test with negative amount
        exception = assertThrows(StatusCodeException.class, () ->
                bankController.handleTransfer("email@example.com", UUID.randomUUID().toString(), -100.0)
        );
        assertEquals(StatusCode.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    public void testHandleTransfer_InvalidAccounts() {
        // Arrange
        String fromEmail = "from@example.com";
        String toIdString = UUID.randomUUID().toString();
        double amount = 100.0;

        // Create a spy for DatabaseController to avoid actual database calls
        try (MockedStatic<DatabaseController> mockedDbController = mockStatic(DatabaseController.class)) {
            mockedDbController.when(() -> new DatabaseController(mockDataSource)).thenReturn(mockDbController);

            // Test with invalid source account
            when(mockDbController.getIDfromEmail(fromEmail)).thenReturn(null);

            StatusCodeException exception = assertThrows(StatusCodeException.class, () ->
                    bankController.handleTransfer(fromEmail, toIdString, amount)
            );
            assertEquals(StatusCode.BAD_REQUEST, exception.getStatusCode());
        }
    }

    @Test
    public void testHandleTransfer_SameAccount() {
        // Arrange
        String fromEmail = "from@example.com";
        UUID id = UUID.randomUUID();
        String toIdString = id.toString();
        double amount = 100.0;

        // Create a spy for DatabaseController to avoid actual database calls
        try (MockedStatic<DatabaseController> mockedDbController = mockStatic(DatabaseController.class)) {
            mockedDbController.when(() -> new DatabaseController(mockDataSource)).thenReturn(mockDbController);

            when(mockDbController.getIDfromEmail(fromEmail)).thenReturn(id);

            // Act & Assert
            StatusCodeException exception = assertThrows(StatusCodeException.class, () ->
                    bankController.handleTransfer(fromEmail, toIdString, amount)
            );
            assertEquals(StatusCode.BAD_REQUEST, exception.getStatusCode());
        }
    }

    @Test
    public void testHandleTransfer_InsufficientFunds() {
        // Arrange
        String fromEmail = "from@example.com";
        UUID fromId = UUID.randomUUID();
        String toIdString = UUID.randomUUID().toString();
        double amount = 2000.0; // More than balance

        // Create a spy for DatabaseController to avoid actual database calls
        try (MockedStatic<DatabaseController> mockedDbController = mockStatic(DatabaseController.class)) {
            mockedDbController.when(() -> new DatabaseController(mockDataSource)).thenReturn(mockDbController);

            when(mockDbController.getIDfromEmail(fromEmail)).thenReturn(fromId);
            when(mockDbController.getBalanceFromID(fromId)).thenReturn("1000.00");

            // Act & Assert
            StatusCodeException exception = assertThrows(StatusCodeException.class, () ->
                    bankController.handleTransfer(fromEmail, toIdString, amount)
            );
            assertEquals(StatusCode.BAD_REQUEST, exception.getStatusCode());
        }
    }

    @Test
    public void testSignup() {
        // Act
        ModelAndView result = bankController.signup();

        // Assert
        assertEquals("signup.hbs", result.getTemplate());
        assertNotNull(result.getModel());
    }

    @Test
    public void testHandleSignup_Success() {
        // Arrange
        String email = "new@example.com";
        String name = "New User";
        String password = "password123";

        // Create a spy for DatabaseController to avoid actual database calls
        try (MockedStatic<DatabaseController> mockedDbController = mockStatic(DatabaseController.class);
             MockedStatic<UUID> mockedUUID = mockStatic(UUID.class)) {

            UUID mockId = UUID.randomUUID();
            mockedUUID.when(() -> UUID.randomUUID()).thenReturn(mockId);

            mockedDbController.when(() -> new DatabaseController(mockDataSource, mockLogger)).thenReturn(mockDbController);

            // Act
            ModelAndView result = bankController.handleSignup(email, name, password);

            // Assert
            verify(mockDbController).createUser(email, name, password, mockId);
            verify(mockDbController).addAccount(any(Account.class));
            assertEquals("login.hbs", result.getTemplate());
        }
    }

    @Test
    public void testHandleSignup_InvalidParams() {
        // Test with null email
        StatusCodeException exception = assertThrows(StatusCodeException.class, () ->
                bankController.handleSignup(null, "Name", "password")
        );
        assertEquals(StatusCode.BAD_REQUEST, exception.getStatusCode());

        // Test with null name
        exception = assertThrows(StatusCodeException.class, () ->
                bankController.handleSignup("email@example.com", null, "password")
        );
        assertEquals(StatusCode.BAD_REQUEST, exception.getStatusCode());

        // Test with null password
        exception = assertThrows(StatusCodeException.class, () ->
                bankController.handleSignup("email@example.com", "Name", null)
        );
        assertEquals(StatusCode.BAD_REQUEST, exception.getStatusCode());

        // Test with empty email
        exception = assertThrows(StatusCodeException.class, () ->
                bankController.handleSignup("", "Name", "password")
        );
        assertEquals(StatusCode.BAD_REQUEST, exception.getStatusCode());

        // Test with empty name
        exception = assertThrows(StatusCodeException.class, () ->
                bankController.handleSignup("email@example.com", "", "password")
        );
        assertEquals(StatusCode.BAD_REQUEST, exception.getStatusCode());

        // Test with empty password
        exception = assertThrows(StatusCodeException.class, () ->
                bankController.handleSignup("email@example.com", "Name", "")
        );
        assertEquals(StatusCode.BAD_REQUEST, exception.getStatusCode());
    }
}

// Helper classes that may be needed for compilation
// Note: These would typically be defined in their own files, but are included here for completeness

class ModelAndView {
    private String template;
    private Map<String, Object> model;

    public ModelAndView(String template, Map<String, Object> model) {
        this.template = template;
        this.model = model;
    }

    public String getTemplate() {
        return template;
    }

    public Map<String, Object> getModel() {
        return model;
    }
}

class Transaction {
    private String description;
    private double amount;

    public Transaction(String description, double amount) {
        this.description = description;
        this.amount = amount;
    }
}

class Account {
    private UUID id;
    private String name;
    private double balance;

    public Account(UUID id, String name, double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }
}

class StatusCodeException extends RuntimeException {
    private final StatusCode statusCode;

    public StatusCodeException(StatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }
}

enum StatusCode {
    BAD_REQUEST
}