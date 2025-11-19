package com.sis.app.model.view;

public class EnrollmentView {
    private final int enrollmentId;
    private final int sectionId;
    private final String courseCode;
    private final String courseTitle;
    private final String dayTime;
    private final String room;
    private final String status;

    public EnrollmentView(int enrollmentId, int sectionId, String courseCode, String courseTitle,
                          String dayTime, String room, String status) {
        this.enrollmentId = enrollmentId;
        this.sectionId = sectionId;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.dayTime = dayTime;
        this.room = room;
        this.status = status;
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

    public String getCourseDisplay() {
        return courseCode + " - " + courseTitle;
    }
}

