package edu.univ.erp.model.view;

public class GradebookRow {
    private final int enrollmentId;
    private final int studentId;
    private final String studentName;
    private final int rollNumber;
    private final Integer midtermScore;
    private final Integer finalExamScore;
    private final Integer projectScore;
    private final Integer finalScore;

    public GradebookRow(int enrollmentId, int studentId, String studentName, int rollNumber,
            Integer midtermScore, Integer finalExamScore,
            Integer projectScore, Integer finalScore) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.midtermScore = midtermScore;
        this.finalExamScore = finalExamScore;
        this.projectScore = projectScore;
        this.finalScore = finalScore;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public int getRollNumber() {
        return rollNumber;
    }

    public Integer getMidtermScore() {
        return midtermScore;
    }

    public Integer getFinalExamScore() {
        return finalExamScore;
    }

    public Integer getProjectScore() {
        return projectScore;
    }

    public Integer getFinalScore() {
        return finalScore;
    }
}
