package edu.univ.erp.model.view;

public class CourseSectionView {
    private String courseCode;
    private String courseTitle;
    private int credits;
    private int sectionId;
    private String dayTime;
    private String room;
    private Integer instructorId;
    private String instructorName;
    private int capacity;
    private int seatsTaken;
    private int semester;
    private int year;

    public CourseSectionView(String courseCode, String courseTitle, int credits, int sectionId,
            String dayTime, String room, Integer instructorId, String instructorName, int capacity,
            int seatsTaken, int semester, int year) {
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.credits = credits;
        this.sectionId = sectionId;
        this.dayTime = dayTime;
        this.room = room;
        this.instructorId = instructorId;
        this.instructorName = instructorName;
        this.capacity = capacity;
        this.seatsTaken = seatsTaken;
        this.semester = semester;
        this.year = year;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public int getCredits() {
        return credits;
    }

    public int getSectionId() {
        return sectionId;
    }

    public String getDayTime() {
        return dayTime;
    }

    public String getRoom() {
        return room;
    }

    public Integer getInstructorId() {
        return instructorId;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getSeatsTaken() {
        return seatsTaken;
    }

    public int getSeatsAvailable() {
        return Math.max(0, capacity - seatsTaken);
    }

    public int getSemester() {
        return semester;
    }

    public int getYear() {
        return year;
    }

    public String getCourseDisplay() {
        return courseCode + " - " + courseTitle;
    }
}
