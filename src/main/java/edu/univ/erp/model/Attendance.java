package edu.univ.erp.model;

import java.time.LocalDate;

public class Attendance {
    private int attendanceId;
    private int enrollmentId;
    private LocalDate date;
    private String status; // PRESENT, ABSENT, LATE, EXCUSED

    public Attendance() {
    }

    public Attendance(int attendanceId, int enrollmentId, LocalDate date, String status) {
        this.attendanceId = attendanceId;
        this.enrollmentId = enrollmentId;
        this.date = date;
        this.status = status;
    }

    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
