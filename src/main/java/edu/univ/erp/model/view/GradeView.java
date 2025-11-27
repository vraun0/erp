package edu.univ.erp.model.view;

public class GradeView {
    private final String courseCode;
    private final String courseTitle;
    private final int sectionId;
    private final int component;
    private final int score;
    private final Integer finalScore;

    public GradeView(String courseCode, String courseTitle, int sectionId, int component,
                     int score, Integer finalScore) {
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.sectionId = sectionId;
        this.component = component;
        this.score = score;
        this.finalScore = finalScore;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public int getSectionId() {
        return sectionId;
    }

    public int getComponent() {
        return component;
    }

    public int getScore() {
        return score;
    }

    public Integer getFinalScore() {
        return finalScore;
    }

    public String getCourseDisplay() {
        return courseCode + " - " + courseTitle;
    }
}

