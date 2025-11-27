package edu.univ.erp.model.view;

public class EnrollmentView {
    private final int enrollmentId;
    private final int sectionId;
    private final String courseCode;
    private final String courseTitle;
    private final String dayTime;
    private final String room;
    private final String status;
    private final String instructorName;
    private final java.time.LocalDate dropDeadline;

    public EnrollmentView(int enrollmentId, int sectionId, String courseCode, String courseTitle,
            String dayTime, String room, String status, String instructorName, java.time.LocalDate dropDeadline) {
        this.enrollmentId = enrollmentId;
        this.sectionId = sectionId;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.dayTime = dayTime;
        this.room = room;
        this.status = status;
        this.instructorName = instructorName;
        this.dropDeadline = dropDeadline;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public String getDayTime() {
        return dayTime;
    }

    public String getRoom() {
        return room;
    }

    public String getStatus() {
        return status;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public java.time.LocalDate getDropDeadline() {
        return dropDeadline;
    }

    public String getCourseDisplay() {
        return courseCode + " - " + courseTitle;
    }
}
