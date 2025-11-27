package edu.univ.erp.service;

import edu.univ.erp.dao.SettingsDao;
import edu.univ.erp.util.DatabaseSetup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class MaintenanceModeTest {

    private static AdminService adminService;
    private static SettingsDao settingsDao;

    @BeforeAll
    public static void setUp() throws Exception {
        DatabaseSetup.init();
        adminService = new AdminService();
        settingsDao = new SettingsDao();
    }

    @Test
    public void testToggleMaintenanceMode() throws SQLException, ServiceException {
        // Ensure initially false
        settingsDao.setMaintenanceMode(false);
        assertFalse(adminService.isMaintenanceMode());

        // Enable
        adminService.toggleMaintenanceMode(true);
        assertTrue(adminService.isMaintenanceMode());
        assertTrue(settingsDao.isMaintenanceMode());

        // Disable
        adminService.toggleMaintenanceMode(false);
        assertFalse(adminService.isMaintenanceMode());
        assertFalse(settingsDao.isMaintenanceMode());
    }
}
