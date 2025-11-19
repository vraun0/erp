package com.sis.app.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DatabaseManager maintains separate connection pools for AuthDB and ERPDB.
 */
public class DatabaseManager {
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String AUTH_DB_NAME = "AuthDB";
    private static final String ERP_DB_NAME = "ERPDB";

    private static DatabaseManager instance;

    private final HikariDataSource authDataSource;
    private final HikariDataSource erpDataSource;

    private DatabaseManager() {
        String user = System.getenv().getOrDefault("SIS_DB_USER", "root");
        String password = System.getenv().getOrDefault("SIS_DB_PASSWORD", "yeahboi123");
        String host = System.getenv().getOrDefault("SIS_DB_HOST", DEFAULT_HOST);
        String port = System.getenv().getOrDefault("SIS_DB_PORT", DEFAULT_PORT);

        this.authDataSource = createDataSource(host, port, AUTH_DB_NAME, user, password);
        this.erpDataSource = createDataSource(host, port, ERP_DB_NAME, user, password);
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private HikariDataSource createDataSource(String host, String port, String dbName, String user, String password) {
        Properties props = new Properties();
        props.setProperty("jdbcUrl", String.format("jdbc:mariadb://%s:%s/%s", host, port, dbName));
        props.setProperty("username", user);
        props.setProperty("password", password);
        props.setProperty("driverClassName", "org.mariadb.jdbc.Driver");

        props.setProperty("maximumPoolSize", "10");
        props.setProperty("minimumIdle", "2");
        props.setProperty("connectionTimeout", "30000");
        props.setProperty("idleTimeout", "600000");
        props.setProperty("maxLifetime", "1800000");
        props.setProperty("autoCommit", "true");
        props.setProperty("poolName", "SIS-" + dbName + "-Pool");

        HikariConfig config = new HikariConfig(props);
        return new HikariDataSource(config);
    }

    public Connection getAuthConnection() throws SQLException {
        return authDataSource.getConnection();
    }

    public Connection getErpConnection() throws SQLException {
        return erpDataSource.getConnection();
    }

    public void close() {
        if (authDataSource != null && !authDataSource.isClosed()) {
            authDataSource.close();
        }
        if (erpDataSource != null && !erpDataSource.isClosed()) {
            erpDataSource.close();
        }
    }
}
