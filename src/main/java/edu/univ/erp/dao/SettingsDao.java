package edu.univ.erp.dao;

import edu.univ.erp.config.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsDao {
    private static final String SELECT_MAINTENANCE = "SELECT maintenance FROM settings WHERE id = 1";
    private static final String UPDATE_MAINTENANCE = "UPDATE settings SET maintenance = ? WHERE id = 1";

    private final DatabaseManager databaseManager;

    public SettingsDao() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    public boolean isMaintenanceMode() throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_MAINTENANCE);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getBoolean("maintenance");
            }
        }
        return false;
    }

    public void setMaintenanceMode(boolean maintenance) throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_MAINTENANCE)) {
            stmt.setBoolean(1, maintenance);
            stmt.executeUpdate();
        }
    }
}
