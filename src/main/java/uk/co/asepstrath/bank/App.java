package uk.co.asepstrath.bank;

import io.jooby.netty.NettyServer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import io.jooby.Jooby;
import io.jooby.handlebars.HandlebarsModule;
import io.jooby.helper.UniRestExtension;
import io.jooby.hikari.HikariModule;
import org.slf4j.Logger;
import javax.sql.DataSource;
import java.io.*;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class App extends Jooby {

    public static final List<Account> accounts = new ArrayList<>(); // Temporary account data
    public static final List<Transaction> transactions = new ArrayList<>(); // Temporary transaction data

    {
        /*
        This section is used for setting up the Jooby Framework modules
         */
        install(new NettyServer());
        install(new UniRestExtension());
        install(new HandlebarsModule());
        install(new HikariModule("mem"));
        
        /*
        This will host any files in src/main/resources/assets on <host>/assets
        For example in the dice template (dice.hbs) it references "assets/dice.png" which is in resources/assets folder
         */
        assets("/assets/*", "/assets");
        assets("/service_worker.js", "/service_worker.js");

        /*
        Now we set up our controllers and their dependencies
         */
        DataSource ds = require(DataSource.class);
        Logger log = getLog();

        mvc(new BankController_(ds, log));

        /*
        Finally we register our application lifecycle methods
         */
        onStarted(() -> onStart());
        onStop(() -> onStop());
    }


    public static void main(final String[] args) {
        runApp(args, App::new);
    }

    /*
    This function will be called when the application starts up,
    it should be used to ensure that the DB is properly setup
     */
    public void onStart() {
        Logger log = getLog();
        log.info("Starting Up...");

        try {
            URL url = new URL("https://api.asep-strath.co.uk/api/accounts");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            }

            StringBuilder inline = new StringBuilder();
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNext()) {
                inline.append(scanner.nextLine());
            }
            scanner.close();


            JsonReader jsonReader = Json.createReader(new StringReader(inline.toString()));
            JsonArray jsonArray = jsonReader.readArray();
            jsonReader.close();

            for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
                UUID id = UUID.fromString(jsonObject.getString("id"));
                accounts.add(new Account(id, jsonObject.getString("name"), jsonObject.getJsonNumber("startingBalance").doubleValue()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            for (int i = 0; i < 154; i++) {

            // URL of the API
            String urlString = "https://api.asep-strath.co.uk/api/transactions?page=" + i;
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read the response body
            String responseBody = new Scanner(connection.getInputStream()).useDelimiter("\\A").next();

            // Parse the XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(responseBody)));

            // Normalize the XML structure
            document.getDocumentElement().normalize();

            // Get the root <pageResult> element
            Element pageResult = document.getDocumentElement();

            // Get all <results> elements under <pageResult>
            NodeList resultsList = pageResult.getElementsByTagName("results");
            if (resultsList.getLength() == 0) {
                continue; // Skip to the next page
            }

            // Iterate through <results> elements
            for (int x = 0; x < resultsList.getLength(); x++) {
                Node node = resultsList.item(x);

                // Check if the <results> element has the correct type
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                // Check if the xsi:type attribute is "transactionModel"
                String typeAttribute = element.getAttribute("xsi:type");
                if ("transactionModel".equals(typeAttribute)) {
                    try {
                    // Extract transaction data with null checks
                    Node idNode = element.getElementsByTagName("id").item(0);
                    Node fromNode = element.getElementsByTagName("from").item(0);
                    Node toNode = element.getElementsByTagName("to").item(0);
                    Node amountNode = element.getElementsByTagName("amount").item(0);
                    Node timestampNode = element.getElementsByTagName("timestamp").item(0);
                    Node typeNode = element.getElementsByTagName("type").item(0);

                    // Get text content of each field
                    UUID id = UUID.fromString(idNode.getTextContent());
                    String from = fromNode != null ? fromNode.getTextContent() : null;
                    String to = toNode != null ? toNode.getTextContent() : null;
                    double amount = Double.parseDouble(amountNode.getTextContent());
                    String date = timestampNode.getTextContent();
                    String type = typeNode.getTextContent();

                    // Add transaction to the list
                    transactions.add(new Transaction(id, amount, date, from, to, type));
                    } catch (Exception e) {
                    System.err.println("Error processing transaction on page " + i + ": " + e.getMessage());
                    }
                }
                }
            }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error parsing XML: " + e.getMessage());
        }


        // Fetch DB Source
        DataSource ds = require(DataSource.class);

        // Create Database Controller and setup the database
        DatabaseController dbController = new DatabaseController(ds, log);
        dbController.setupDatabase();

        // creating test user connected to already existing account from api
        dbController.createUser("test@scotbank.com", "Melva Rogahn", "test", UUID.fromString("04f6ab33-8208-4234-aabd-b6a8be8493da"));
    }

    /*
    This function will be called when the application shuts down
     */
    public void onStop() {
        System.out.println("Shutting Down...");
    }
}
