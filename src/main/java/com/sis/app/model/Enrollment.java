package com.sis.app.model;

/**
 * Enrollment record mapped to ERPDB.enrollments
 */
public class Enrollment {
    private int enrollmentId;
    private String studentId;   // references students.user_id
    private int sectionId;      // references sections.section_id
    private String status;      // ENROLLED, DROPPED, COMPLETED

    public Enrollment() {
    }

    public Enrollment(int enrollmentId, String studentId, int sectionId, String status) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.status = status;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return "ENROLLED".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "enrollmentId=" + enrollmentId +
                ", studentId='" + studentId + '\'' +
                ", sectionId=" + sectionId +
                ", status='" + status + '\'' +
                '}';
    }
}


