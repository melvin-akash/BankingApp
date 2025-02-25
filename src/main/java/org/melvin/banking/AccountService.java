package org.melvin.banking;

import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class AccountService {

    private static final String dbUrl = "jdbc:sqlite:";
    private static String dbFile;
    private Connection con = null;

    final static Scanner sc = new Scanner(System.in);
    private static final String CREATE_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS card (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            number TEXT NOT NULL UNIQUE,
            pin TEXT NOT NULL,
            balance INTEGER DEFAULT 0
        );
        """;
//    private final BankLoginDetails[] bankLoginDetailsDatabase = new BankLoginDetails[100];
//    private BankLoginDetails currentUserDetails;
    private Card currentCardDetails;
//    private int current = 0;
    private int transferCardBalance;

    public boolean menu() {
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");

        int option = sc.nextInt();
        
        switch (option){
            case 1 : 
                createAccount();
                break;
            case 2 :
                logIn();
                break;
            case 0 :
                // exit logic
                System.out.println("Bye!");

                return false;
            default: 
                System.out.println("Invalid input");
                menu();
        }
        return true;
    }

    private void logIn() {
        System.out.println("Enter your card number:");
        long cardNumber = sc.nextLong();

        boolean cardExists = cardNumberExistsInDB(String.valueOf(cardNumber));
        String output = cardExists ? "You have successfully logged in!" : "Wrong card number or PIN!";
        System.out.println(output);

        //login successful
        //display menu
        if(!cardExists){
            menu();
        }
        loginMenu(currentCardDetails);

    }

    private void loginMenu(Card currentCardDetails) {
        System.out.println("1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. CLose account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");

        int option = sc.nextInt();

        switch (option){
            case 1:
                System.out.println("Balance: " + currentCardDetails.getBalance());
                loginMenu(currentCardDetails);
            case 2:
                addIncome();
                loginMenu(currentCardDetails);
            case 3:
                transfer();
                loginMenu(currentCardDetails);
            case 4:
                closeAccount();
                System.out.println("The account has been closed!");
            case 5:
                menu();
            case 0:
                closeConnection();
                System.out.println("Bye!");
                System.exit(1);
            default:
                System.out.println("Invalid option");
                loginMenu(currentCardDetails);
        }

    }

    private void closeAccount() {
        try (PreparedStatement statement = con.prepareStatement("DELETE FROM card WHERE number = ?")) {

            statement.setString(1, currentCardDetails.getNumber());
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error while deleting account: " + e.getMessage());
        }
    }

    private void transfer() {
        System.out.println("Enter card number:");  // card number to transfer amount
        String toCardNumber = sc.next();

        boolean cardExists = cardNumberCheck(toCardNumber);
        if(cardExists) {

            System.out.println("Enter how much money you want to transfer:");   //transfer amount
            int transferAmount = sc.nextInt();

            if (transferAmount > Long.parseLong(currentCardDetails.getBalance())){
                System.out.println("Not enough money!");
                loginMenu(currentCardDetails);
            }

            // debit money from user
            try (PreparedStatement statement = con.prepareStatement("UPDATE card SET BALANCE = ? WHERE number = ?")) {
                con.setAutoCommit(false);
                statement.setString(1, String.valueOf(Long.parseLong(currentCardDetails.getBalance())  - transferAmount));
                statement.setString(2, currentCardDetails.getNumber());
                statement.executeUpdate();

            } catch (SQLException e) {
                System.out.println("Error while debiting " + e.getMessage());
            }

            // credit money to another user
            try (PreparedStatement statement = con.prepareStatement("UPDATE card SET BALANCE = ? WHERE number = ?")) {
                statement.setString(1, "" + transferCardBalance + transferAmount);
                statement.setString(2, toCardNumber);
                statement.executeUpdate();
                con.commit();
                System.out.println("Success!");

            } catch (SQLException e) {
                System.out.println("Error while transferring " + e.getMessage());
            }

        }
        else{
            loginMenu(currentCardDetails);
        }

    }

    private boolean cardNumberCheck(String cardNumber) {

        if(cardNumber.length() != 16 || ! cardNumber.equals(generateCheckSum(cardNumber.substring(0 , cardNumber.length() - 1)))){
            System.out.println("Probably you made a mistake in the card number. Please try again!");
            return false;
        }
        try (PreparedStatement statement = con.prepareStatement("SELECT * FROM card WHERE number = ?")) {
            statement.setString(1, cardNumber);
            ResultSet rs = statement.executeQuery();



            if (!rs.next()) {
                System.out.println("Such a card does not exist.");
                return false;
            }
            transferCardBalance = rs.getInt("balance");

        } catch (SQLException e) {
            System.out.println("Error while checking card number : " + e.getMessage());
            return false;
        }
        return true;
    }

    private void addIncome() {
        System.out.println("Enter income:");
        int income = sc.nextInt();

        long newBalance = Long.parseLong(currentCardDetails.getBalance()) + income;
        currentCardDetails.setBalance("" + newBalance);

        try (PreparedStatement statement = con.prepareStatement("UPDATE card SET BALANCE = ? WHERE number = ?")) {

            statement.setString(1, "" + newBalance);
            statement.setString(2, currentCardDetails.getNumber());
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error while creating account: " + e.getMessage());
        }
        System.out.println("Income was added!");
    }

    private boolean cardNumberExist(long cardNumber) {
//
//        for(BankLoginDetails details : bankLoginDetailsDatabase){
//            if(details != null && details.getCardNumber().equals(String.valueOf(cardNumber))){
//                System.out.println("Enter your PIN:");
//                String pin = sc.next();
//
//                if(pin.equals(details.getPin())){
//                    currentUserDetails = details;
//                    return true;
//                }
//            }
//        }
        return false;
    }

    private boolean cardNumberExistsInDB(String cardNumber) {

        System.out.println("Enter your PIN:");
        String pin = sc.next();

        if(cardNumber.length() != 16 || ! cardNumber.equals(generateCheckSum(cardNumber.substring(0 , cardNumber.length() - 1)))){
            System.out.println("Probably you made a mistake in the card number. Please try again!");
            return false;
        }

        try (PreparedStatement statement = con.prepareStatement("SELECT * FROM card WHERE number = ? AND pin = ?")) {
            statement.setString(1, cardNumber);
            statement.setString(2, pin);
            ResultSet rs = statement.executeQuery();

            if (!rs.next()) {
                System.out.println("Such a card does not exist.");
                return false;
            }

            if(pin.equals(rs.getString("pin"))){
                Card card = new Card(cardNumber , pin , rs.getString("balance"));
                currentCardDetails = card;
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }

        return false;
    }

    private void createAccount() {
        // 400000 493832089 5
        // 400000 1856702315

        String cardNumber = createCustomerCardNo();
        String pin = createPin();

        // write duplicate checking logic
        String query = "SELECT count(*) FROM card WHERE number = ?";
        boolean duplicateExists = false;

        try (PreparedStatement statement = con.prepareStatement(query)) {

            statement.setString(1, cardNumber);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                duplicateExists =  rs.getInt(1) > 0;  // If count > 0, it means the card number already exists
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }

        if(duplicateExists){
            createAccount();
        }
//        BankLoginDetails details = new BankLoginDetails(cardNumber , pin);

        System.out.println("Your card has been created");
        System.out.println("Your card number:\n" + cardNumber);
        System.out.println("Your card PIN:\n" + pin);

        Card card = new Card(cardNumber , pin , String.valueOf(0));

        saveCard(card);
    }

    private void saveCard(Card card) {

        // insert data
        try (PreparedStatement statement = con.prepareStatement("INSERT INTO card (number, pin) VALUES (?, ?)")) {
             statement.setString(1, card.getNumber());
             statement.setString(2, card.getPin());
             statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error while creating account: " + e.getMessage());
        }
    }

    private String createPin() {
        Random rand = new Random();
        int pin = rand.nextInt(10000 - 1000) + 1000;

        return String.valueOf(pin);
    }

    private String createCustomerCardNo() {
        String bin = "400000";

        Random rand = new Random();
        int customerAccNo = rand.nextInt(1000000000 - 100000000) + 100000000;

        String cardNumber = generateCheckSum(bin + customerAccNo);

        return String.valueOf(cardNumber);
    }

    public String generateCheckSum(String s) {
        int[] arr = new int[s.length()];

        for(int i = 0; i < arr.length ; i++){
            arr[i] = Integer.parseInt(String.valueOf(s.charAt(i))) ;
        }

        //multiply odd digits by 2
        for(int i = 0; i < arr.length ; i++){
            if(i % 2 == 0){
                arr[i] *= 2;
            }
        }
        //subtract 9 for values over 9
        for(int i = 0; i < arr.length ; i++){
            if(arr[i] > 9){
                arr[i] -= 9;
            }
        }
        //add all numbers
        int sum = 0;
        for (int j : arr) {
            sum += j;
        }

        int checkSum = 10 - (sum % 10);

        return s.concat(String.valueOf(checkSum));
    }


    public void createDatabase(String dbFile) {
        AccountService.dbFile = dbFile;

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC driver not found: " + e.getMessage());
        }

        try {
            this.con = DriverManager.getConnection(dbUrl + dbFile);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (Statement statement = con.createStatement()){
                statement.execute(CREATE_TABLE_SQL);  // Create the card table if it doesn't exist

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void closeConnection(){

        if (con != null) {
            try {
                con.close();
                con = null; // Reset connection after closing
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }


}
