package uk.co.asepstrath.bank;

import io.jooby.ModelAndView;
import io.jooby.StatusCode;
import io.jooby.annotation.*;
import io.jooby.exception.StatusCodeException;
import io.jooby.handlebars.HandlebarsModule;
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
import java.util.UUID;


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

    

    @GET("/login")
    public ModelAndView login(@QueryParam String email) {

        // we must create a model to pass to the "login" template
        Map<String, Object> model = new HashMap<>();
        model.put("email", email);

        return new ModelAndView("login.hbs", model);
    }

    @GET("/dashboard")
    public ModelAndView dashboard(@QueryParam String email) {
        // we must create a model to pass to the "dashboard" template
        Map<String, Object> model = new HashMap<>();
        DatabaseController dbController = new DatabaseController(dataSource);
        UUID accountID = dbController.getIDfromEmail(email);
        model.put("email", email);
        model.put("name", dbController.getNamefromID(accountID));
        model.put("balance", dbController.getBalanceFromID(accountID));
        model.put("id", accountID);
        model.put("transactions", dbController.getTransactionsById(accountID));
        return new ModelAndView("dashboard.hbs", model);
    }

    @GET("/transfer")
    public ModelAndView transfer(@QueryParam String email) {
        // we must create a model to pass to the "dashboard" template
        Map<String, Object> model = new HashMap<>();
        DatabaseController dbController = new DatabaseController(dataSource);
        UUID accountID = dbController.getIDfromEmail(email);

        model.put("name", dbController.getNamefromID(accountID));
        model.put("balance", dbController.getBalanceFromID(accountID));
        model.put("id", accountID);
        model.put("email", email);
        return new ModelAndView("transfer.hbs", model);
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

    if (Double.parseDouble(dbController.getBalanceFromID(fromAccountID)) < amount){
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

        dbController.createUser(email, name, password, id);

        dbController.addAccount(new Account(id, name, 0));
        
        // Redirect to login page
        Map<String, Object> model = new HashMap<>();
        return new ModelAndView("login.hbs", model);
    }

}


