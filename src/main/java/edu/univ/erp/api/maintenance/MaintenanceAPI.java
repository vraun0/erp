package edu.univ.erp.api.maintenance;

import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.ServiceException;

import java.sql.SQLException;

public class MaintenanceAPI {
    private final AdminService adminService;

    public MaintenanceAPI() {
        this.adminService = new AdminService();
    }

    public ApiResponse<Boolean> isMaintenanceMode() {
        try {
            return ApiResponse.success(adminService.isMaintenanceMode());
        } catch (SQLException e) {
            return ApiResponse.error("Error checking maintenance mode: " + e.getMessage());
        }
    }

    public ApiResponse<Void> setMaintenanceMode(boolean enabled) {
        try {
            adminService.toggleMaintenanceMode(enabled);
            return ApiResponse.success(null);
        } catch (ServiceException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
