package edu.univ.erp.service;

import com.opencsv.CSVWriter;
import edu.univ.erp.model.view.GradeView;
import edu.univ.erp.model.view.GradebookRow;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ExportService {

    public List<GradebookRow> importGradebook(File file)
            throws IOException, com.opencsv.exceptions.CsvValidationException {
        List<GradebookRow> rows = new ArrayList<>();
        try (com.opencsv.CSVReader reader = new com.opencsv.CSVReader(new FileReader(file))) {
            String[] header = reader.readNext(); // Skip header
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length < 7)
                    continue;

                int enrollmentId = Integer.parseInt(line[0]);
                int studentId = Integer.parseInt(line[1]);
                // line[2] is roll number, skip
                Integer midterm = parseScore(line[3]);
                Integer finalExam = parseScore(line[4]);
                Integer project = parseScore(line[5]);
                Integer finalScore = parseScore(line[6]);

                rows.add(new GradebookRow(enrollmentId, studentId, "", 0, midterm, finalExam, project, finalScore));
            }
        }
        return rows;
    }

    private Integer parseScore(String value) {
        if (value == null || value.trim().isEmpty())
            return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void exportGradebook(List<GradebookRow> rows, File file) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            writer.writeNext(
                    new String[] { "Enrollment ID", "Student", "Roll", "Midterm", "Final", "Project", "Final Score" });
            for (GradebookRow row : rows) {
                writer.writeNext(new String[] {
                        String.valueOf(row.getEnrollmentId()),
                        String.valueOf(row.getStudentId()),
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
            writer.writeNext(new String[] { "Course", "Section", "Component", "Score", "Final Score" });
            for (GradeView grade : grades) {
                writer.writeNext(new String[] {
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

    public void exportTranscriptPDF(int studentId, List<GradeView> grades, File file) throws IOException {
        try (com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(file);
                com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
                com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf)) {

            document.add(new com.itextpdf.layout.element.Paragraph("Official Transcript")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

            document.add(new com.itextpdf.layout.element.Paragraph("Student ID: " + studentId)
                    .setFontSize(12)
                    .setMarginBottom(20));

            float[] columnWidths = { 3, 1, 1, 1, 1 };
            com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(
                    com.itextpdf.layout.properties.UnitValue.createPercentArray(columnWidths));
            table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

            table.addHeaderCell("Course");
            table.addHeaderCell("Section");
            table.addHeaderCell("Component");
            table.addHeaderCell("Score");
            table.addHeaderCell("Final Score");

            for (GradeView grade : grades) {
                table.addCell(grade.getCourseDisplay());
                table.addCell(String.valueOf(grade.getSectionId()));
                table.addCell(String.valueOf(grade.getComponent()));
                table.addCell(String.valueOf(grade.getScore()));
                table.addCell(convert(grade.getFinalScore()));
            }

            document.add(table);
        }
    }
}
