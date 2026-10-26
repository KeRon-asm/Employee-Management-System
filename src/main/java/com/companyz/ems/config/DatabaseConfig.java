package com.companyz.ems.config;

import com.companyz.ems.model.Role;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Reads database settings from {@code db.properties} in the working directory
 * (git-ignored; copy {@code db.properties.example} to create it).
 *
 * <p>The app uses a different MySQL account per security level, so a bug in the
 * employee screens can never write to the database.
 */
public final class DatabaseConfig {

    /** Which MySQL account to connect as. */
    public enum Account { AUTH, EMPLOYEE, HR }

    private final Properties props;

    private DatabaseConfig(Properties props) {
        this.props = props;
    }

    public static DatabaseConfig load(Path file) throws IOException {
        if (!Files.exists(file)) {
            throw new IOException("Missing " + file.toAbsolutePath()
                    + " -- copy db.properties.example to db.properties and fill it in.");
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        }
        return new DatabaseConfig(props);
    }

    public String url() {
        return required("db.url");
    }

    public String user(Account account) {
        return required(prefix(account) + ".user");
    }

    public String password(Account account) {
        return required(prefix(account) + ".password");
    }

    /** The account a signed-in user's work runs under. */
    public static Account accountFor(Role role) {
        return role == Role.HR_ADMIN ? Account.HR : Account.EMPLOYEE;
    }

    private static String prefix(Account account) {
        return "db." + account.name().toLowerCase();
    }

    private String required(String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("db.properties is missing " + key);
        }
        return value.trim();
    }
}
