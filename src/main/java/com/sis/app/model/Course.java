package com.sis.app.model;

/**
 * Course record mapped to ERPDB.courses
 */
public class Course {
    private String code;   // primary key
    private String title;
    private int credits;

    public Course() {
    }

    public Course(String code, String title, int credits) {
        this.code = code;
        this.title = title;
        this.credits = credits;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    @Override
    public String toString() {
        return code + " - " + title;
    }
}


