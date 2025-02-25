package org.melvin.banking;

import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        AccountService accountService = new AccountService();

        // create db if not exists
        accountService.createDatabase(args[1]);
        boolean printMenu = true;

        while(printMenu) {
            printMenu = accountService.menu();
        }

    }

}
