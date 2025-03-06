package uk.co.asepstrath.bank;

import io.jooby.ModelAndView;
import io.jooby.StatusCode;
import io.jooby.annotation.*;
import io.jooby.exception.StatusCodeException;
import kong.unirest.core.Unirest;

import org.h2.engine.Database;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.App;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/*
    Example Controller is a Controller from the MVC paradigm.
    The @Path Annotation will tell Jooby what /path this Controller can respond to,
    in this case the controller will respond to requests from <host>/example
 */
@Path("/scotbank")
public class BankController {

    private final DataSource dataSource;
    private final Logger logger;

    /*
    This constructor can take in any dependencies the controller may need to respond to a request
     */
    public BankController(DataSource ds, Logger log) {
        dataSource = ds;
        logger = log;
    }

    /*
    This is the simplest action a controller can perform
    The @GET annotation denotes that this function should be invoked when a GET HTTP request is sent to <host>/example
    The returned string will then be sent to the requester
     */
    @GET
    public String welcome() {
        return "Welcome to Jooby!";
    }

    @GET("/accounts")
    public String listAccounts() {
        StringBuilder output = new StringBuilder();
        output.append("Account List");
        output.append("\n");

        for (Account account : App.accounts) {
            output.append(account.toString()).append("\n");
        }

        return output.toString();
    }

    @GET("/transactions")
    public String listTransactions() {
        StringBuilder output = new StringBuilder();
        output.append("Transaction List");
        output.append("\n");

        for (Transaction transaction : App.transactions) {
            output.append(transaction.toString()).append("\n");
        }

        return output.toString();
    }

    @GET("/login")
    public ModelAndView login(@QueryParam String name) {

        // If no name has been sent within the query URL
        if (name == null) {
            name = "Your";
        } else {
            name = name + "'s";
        }

        // we must create a model to pass to the "login" template
        Map<String, Object> model = new HashMap<>();
        model.put("name", name);

        return new ModelAndView("login.hbs", model);
    }

    @GET("/dashboard")
    public ModelAndView dashboard(@QueryParam String name) {
        // we must create a model to pass to the "dashboard" template
        Map<String, Object> model = new HashMap<>();
        model.put("name", name);
        DatabaseController dbController = new DatabaseController(dataSource);
        model.put("balance", dbController.getBalanceFromName(name));

        return new ModelAndView("dashboard.hbs", model);
    }

    @GET("/signup")
    public ModelAndView signup() {
        Map<String, Object> model = new HashMap<>();
        return new ModelAndView("signup.hbs",model);
    }

    @POST("/signup")
    public ModelAndView handleSignup(@FormParam String name, @FormParam String password) {
        if (name == null || password == null || name.isEmpty() || password.isEmpty()) {
            throw new StatusCodeException(StatusCode.BAD_REQUEST, "Name and password are required.");
        }

        // Create new account (for simplicity, starting balance is 0)
        Account newAccount = new Account(name, 0.0);

        // Add account to the database
        DatabaseController dbController = new DatabaseController(dataSource, logger);
        dbController.addAccount(newAccount);

        // Redirect to login page
        Map<String, Object> model = new HashMap<>();
        model.put("message", "Account created successfully! You can now log in.");
        return new ModelAndView("login.hbs", model);
    }

}


