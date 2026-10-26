package com.companyz.ems;

import com.companyz.ems.config.ConnectionFactory;
import com.companyz.ems.config.DatabaseConfig;
import com.companyz.ems.config.DatabaseConfig.Account;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Entry point. For now it checks that each database account can connect,
 * so every teammate can confirm their setup with: {@code mvn -q compile exec:java}
 * The login screen replaces this once the UI is built.
 */
public final class App {

    private App() {
    }

    public static void main(String[] args) {
        try {
            DatabaseConfig config = DatabaseConfig.load(Path.of("db.properties"));
            ConnectionFactory connections = new ConnectionFactory(config);
            System.out.println("Company Z Employee Management System -- setup check");
            System.out.println("Database: " + config.url());

            boolean allOk = true;
            for (Account account : Account.values()) {
                allOk &= check(connections, account);
            }
            System.out.println(allOk ? "All accounts connected. You're set up."
                                     : "Some accounts failed. See README > Troubleshooting.");
            if (!allOk) {
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Setup check failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static boolean check(ConnectionFactory connections, Account account) {
        try (Connection c = connections.open(account);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT CURRENT_USER()")) {
            rs.next();
            System.out.printf("  [ OK ] %-8s connected as %s%n", account, rs.getString(1));
            return true;
        } catch (Exception e) {
            System.out.printf("  [FAIL] %-8s %s%n", account, e.getMessage());
            return false;
        }
    }
}
