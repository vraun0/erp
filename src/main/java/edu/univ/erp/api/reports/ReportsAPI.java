package edu.univ.erp.api.reports;

import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.service.ExportService;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.ServiceException;
import edu.univ.erp.model.view.GradebookRow;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ReportsAPI {
    private final ExportService exportService;
    private final InstructorService instructorService;

    public ReportsAPI() {
        this.exportService = new ExportService();
        this.instructorService = new InstructorService();
    }

    public ApiResponse<Void> exportClassList(int instructorId, int sectionId, File destination) {
        try {
            List<GradebookRow> rows = instructorService.getGradebook(instructorId, sectionId);
            exportService.exportGradebook(rows, destination);
            return ApiResponse.success(null);
        } catch (SQLException | ServiceException | IOException e) {
            return ApiResponse.error("Export failed: " + e.getMessage());
        }
    }
}
