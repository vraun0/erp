package com.sis.app.service;

import com.opencsv.CSVWriter;
import com.sis.app.model.view.GradeView;
import com.sis.app.model.view.GradebookRow;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ExportService {

    public void exportGradebook(List<GradebookRow> rows, File file) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            writer.writeNext(new String[]{"Enrollment ID", "Student", "Roll", "Midterm", "Final", "Project", "Final Score"});
            for (GradebookRow row : rows) {
                writer.writeNext(new String[]{
                        String.valueOf(row.getEnrollmentId()),
                        row.getStudentId(),
                        String.valueOf(row.getRollNumber()),
                        convert(row.getMidtermScore()),
                        convert(row.getFinalExamScore()),
                        convert(row.getProjectScore()),
                        convert(row.getFinalScore())
                });
            }
        }
    }

    public void exportStudentGrades(List<GradeView> grades, File file) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            writer.writeNext(new String[]{"Course", "Section", "Component", "Score", "Final Score"});
            for (GradeView grade : grades) {
                writer.writeNext(new String[]{
                        grade.getCourseDisplay(),
                        String.valueOf(grade.getSectionId()),
                        String.valueOf(grade.getComponent()),
                        String.valueOf(grade.getScore()),
                        convert(grade.getFinalScore())
                });
            }
        }
    }

    private String convert(Integer value) {
        return value == null ? "" : String.valueOf(value);
    }
}

