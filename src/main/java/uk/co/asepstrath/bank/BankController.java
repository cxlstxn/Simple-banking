package uk.co.asepstrath.bank;

import io.jooby.Context;
import io.jooby.ModelAndView;
import io.jooby.StatusCode;
import io.jooby.annotation.*;
import io.jooby.exception.StatusCodeException;

import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/scotbank")
public class BankController {

    private final DataSource dataSource;
    private final Logger logger;

    public BankController(DataSource ds, Logger log) {
        dataSource = ds;
        logger = log;
    }

    @GET("/login")
    public ModelAndView login(Context ctx) {
        ctx.session().destroy();
        Map<String, Object> model = new HashMap<>();
        return new ModelAndView("login.hbs", model);
    }


    @POST("/login")
    public ModelAndView handleLogin(@FormParam String email, @FormParam String password, Context ctx) {
        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            throw new StatusCodeException(StatusCode.BAD_REQUEST, "Email and password are required.");
        }


        DatabaseController dbController = new DatabaseController(dataSource);

        if (dbController.emailExists(email)) {
            if (dbController.verifyPassword(password, dbController.getPasswordFromEmail(email))) {
                ctx.session().put("email", email);
                ctx.setResponseCode(StatusCode.OK_CODE);
                ctx.sendRedirect("/scotbank/dashboard");
                return null;
            } else {
                throw new StatusCodeException(StatusCode.BAD_REQUEST, "Incorrect password.");
            }
        } else {
            throw new StatusCodeException(StatusCode.BAD_REQUEST, "Email not found.");
        }
    }

    @GET("/dashboard")
    public ModelAndView dashboard(Context ctx) {
        String email = ctx.session().get("email").valueOrNull();
        if (email == null) {
            ctx.sendRedirect("/scotbank/login");
            ctx.session().destroy();
            return null;
        }

        Map<String, Object> model = new HashMap<>();
        DatabaseController dbController = new DatabaseController(dataSource);

        if (dbController.getRoleFromEmail(email).equals("admin")) {
            ctx.sendRedirect("/scotbank/adminDashboard");
            return null;
        }

        UUID accountID = dbController.getIDfromEmail(email);
        List<Transaction> transactions = dbController.getTransactionsById(accountID);

        for (Transaction transaction : transactions) {
            if (transaction.getFrom() != null) {
                transaction.setFrom(dbController.getNamefromID(UUID.fromString(transaction.getFrom())));
            }

            if (transaction.getTo() != null) {
                if (transaction.getTo().length() <= 3) {
                    transaction.setTo(dbController.getBusinessName(transaction.getTo()));
                } else {
                    transaction.setTo(dbController.getNamefromID(UUID.fromString(transaction.getTo())));
                }
            }
                
        }
        
        model.put("email", email);
        model.put("name", dbController.getNamefromID(accountID));
        model.put("balance", dbController.getBalanceFromID(accountID));
        model.put("id", accountID);
        model.put("transactions", transactions);
        return new ModelAndView("dashboard.hbs", model);
    }

    @GET("/transfer")
    public ModelAndView transfer(Context ctx) {
        String email = ctx.session().get("email").valueOrNull();
        if (email == null) {
            ctx.sendRedirect("/scotbank/login");
            ctx.session().destroy();
            return null;
        }

        Map<String, Object> model = new HashMap<>();
        DatabaseController dbController = new DatabaseController(dataSource);
        UUID accountID = dbController.getIDfromEmail(email);

        model.put("name", dbController.getNamefromID(accountID));
        model.put("balance", dbController.getBalanceFromID(accountID));
        model.put("id", accountID);
        model.put("email", email);
        return new ModelAndView("transfer.hbs", model);
    }


    @GET("/roundUp")
    public ModelAndView roundUp(Context ctx) {
        String email = ctx.session().get("email").valueOrNull();
        if (email == null) {
            ctx.sendRedirect("/scotbank/login");
            ctx.session().destroy();
            return null;
        }

        Map<String, Object> model = new HashMap<>();
        DatabaseController dbController = new DatabaseController(dataSource);
        UUID accountID = dbController.getIDfromEmail(email);

        model.put("name", dbController.getNamefromID(accountID));
        model.put("balance", dbController.getBalanceFromID(accountID));
        model.put("id", accountID);

        return new ModelAndView("roundUp.hbs", model);
    }


    @POST("/transfer")
    public ModelAndView handleTransfer(@FormParam String email, @FormParam String to, @FormParam double amount) {
        if (email == null || to == null || amount <= 0) {
            throw new StatusCodeException(StatusCode.BAD_REQUEST, "Invalid transfer details.");
        }

        DatabaseController dbController = new DatabaseController(dataSource);
        UUID fromAccountID = dbController.getIDfromEmail(email);
        UUID toAccountID = UUID.fromString(to);

        if (fromAccountID == null || toAccountID == null) {
            throw new StatusCodeException(StatusCode.BAD_REQUEST, "Invalid account details.");
        }

        if (fromAccountID.equals(toAccountID)) {
            throw new StatusCodeException(StatusCode.BAD_REQUEST, "Cannot transfer to the same account.");
        }

        if (Double.parseDouble(dbController.getBalanceFromID(fromAccountID)) < amount) {
            throw new StatusCodeException(StatusCode.BAD_REQUEST, "Insufficient funds.");
        }

        dbController.transferFunds(fromAccountID, toAccountID, amount);

    Map<String, Object> model = new HashMap<>();
    model.put("email", email);
    model.put("name", dbController.getNamefromID(fromAccountID));
    model.put("balance", dbController.getBalanceFromID(fromAccountID));
    model.put("id", fromAccountID);
    model.put("transactions", dbController.getTransactionsById(fromAccountID));

    return new ModelAndView("dashboard.hbs", model);
    }

    @GET("/signup")
    public ModelAndView signup() {
    Map<String, Object> model = new HashMap<>();
    return new ModelAndView("signup.hbs",model);
    }

    @POST("/signup")
    public ModelAndView handleSignup(@FormParam String email, @FormParam String name, @FormParam String password) {
        if (email == null || name == null || password == null || email.isEmpty() || name.isEmpty() || password.isEmpty()) {
            throw new StatusCodeException(StatusCode.BAD_REQUEST, "Name and password are required.");
        }
        DatabaseController dbController = new DatabaseController(dataSource, logger);
        UUID id = UUID.randomUUID();

        dbController.createUser(email, name, password, "user", id);

        dbController.addAccount(new Account(id, name, 0, false , "EH1 1AA"));
        
        // Redirect to login page
        Map<String, Object> model = new HashMap<>();
        return new ModelAndView("login.hbs", model);
    }


    @GET("/adminDashboard")
    public ModelAndView adminDashboard(Context ctx) {
        String email = ctx.session().get("email").valueOrNull();
        if (email == null) {
            ctx.sendRedirect("/scotbank/login");
            ctx.session().destroy();
            return null;
        }

        Map<String, Object> model = new HashMap<>();
        DatabaseController dbController = new DatabaseController(dataSource);
        if (!dbController.getRoleFromEmail(email).equals("admin")) {
            ctx.sendRedirect("/scotbank/login");
            ctx.session().destroy();
            return null;
        }
        return new ModelAndView("adminDashboard.hbs", model);
    }

    @GET("/adminTransactions")
    public ModelAndView adminTransactions(Context ctx) {
        String email = ctx.session().get("email").valueOrNull();
        if (email == null) {
            ctx.sendRedirect("/scotbank/login");
            ctx.session().destroy();
            return null;
        }

        Map<String, Object> model = new HashMap<>();
        DatabaseController dbController = new DatabaseController(dataSource);

        if (!dbController.getRoleFromEmail(email).equals("admin")) {
            ctx.sendRedirect("/scotbank/login");
            ctx.session().destroy();
            return null;
        }
        List<Transaction> transactions = dbController.getAllTransactions();

        model.put("transactions", transactions);
        return new ModelAndView("adminTransactions.hbs", model);
    }

    @GET("/adminSanctioned")
    public ModelAndView adminSanctioned(Context ctx) {
        String email = ctx.session().get("email").valueOrNull();
        if (email == null) {
            ctx.sendRedirect("/scotbank/login");
            ctx.session().destroy();
            return null;
        }

        Map<String, Object> model = new HashMap<>();
        DatabaseController dbController = new DatabaseController(dataSource);

        if (!dbController.getRoleFromEmail(email).equals("admin")) {
            ctx.sendRedirect("/scotbank/login");
            ctx.session().destroy();
            return null;
        }
        List<Transaction> transactions = dbController.getSanctionedTransactions();

        model.put("transactions", transactions);
        return new ModelAndView("adminSanctioned.hbs", model);
    }


    @GET("/adminAccounts")
    public ModelAndView adminAccounts(Context ctx) {
        String email = ctx.session().get("email").valueOrNull();
        if (email == null) {
            ctx.sendRedirect("/scotbank/login");
            ctx.session().destroy();
            return null;
        }

        Map<String, Object> model = new HashMap<>();
        DatabaseController dbController = new DatabaseController(dataSource);

        if (!dbController.getRoleFromEmail(email).equals("admin")) {
            ctx.sendRedirect("/scotbank/login");
            ctx.session().destroy();
            return null;
        }
        List<Account> accounts = dbController.getAllAccounts();

        model.put("accounts", accounts);
        return new ModelAndView("adminAccounts.hbs", model);
    }


    @GET("/adminTen")
    public ModelAndView adminTen(Context ctx) {
        String email = ctx.session().get("email").valueOrNull();
        if (email == null) {
            ctx.sendRedirect("/scotbank/login");
            ctx.session().destroy();
            return null;
        }

        Map<String, Object> model = new HashMap<>();
        DatabaseController dbController = new DatabaseController(dataSource);

        if (!dbController.getRoleFromEmail(email).equals("admin")) {
            ctx.sendRedirect("/scotbank/login");
            ctx.session().destroy();
            return null;
        }
        List<Account> accounts = dbController.getTopTenBiggestSpenders();

        model.put("accounts", accounts);
        return new ModelAndView("adminten.hbs", model);
    }

}


