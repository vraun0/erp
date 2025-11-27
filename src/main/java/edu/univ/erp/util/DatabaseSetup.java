package edu.univ.erp.util;

import edu.univ.erp.config.DatabaseManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseSetup {

    public static void init() {
        try {
            System.out.println("Initializing databases...");
            initAuthDb();
            initErpDb();
            System.out.println("Database initialization complete.");
        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initAuthDb() throws SQLException, IOException {
        String sql = loadResource("/auth_schema.sql");
        try (Connection conn = DatabaseManager.getInstance().getAuthConnection();
             Statement stmt = conn.createStatement()) {
            executeScript(stmt, sql);
        }
    }

    private static void initErpDb() throws SQLException, IOException {
        String sql = loadResource("/erp_schema.sql");
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             Statement stmt = conn.createStatement()) {
            executeScript(stmt, sql);
        }
    }

    private static String loadResource(String path) throws IOException {
        try (InputStream is = DatabaseSetup.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("Resource not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
    }

    private static void executeScript(Statement stmt, String sql) throws SQLException {
        // Simple splitter for ;
        // Note: This is a basic implementation. For complex scripts with triggers/procedures, 
        // a more robust parser is needed.
        String[] commands = sql.split(";");
        for (String command : commands) {
            if (command.trim().isEmpty()) {
                continue;
            }
            try {
                stmt.execute(command);
            } catch (SQLException e) {
                System.err.println("Failed to execute command: " + command);
                throw e;
            }
        }
    }
}
