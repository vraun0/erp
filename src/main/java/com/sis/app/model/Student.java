package com.sis.app.model;

/**
 * Student record mapped to ERPDB.students
 */
public class Student {
    private String userId;      // references AuthDB.users_auth.username
    private int rollNumber;
    private String program;
    private int year;

    public Student() {
    }

    public Student(String userId, int rollNumber, String program, int year) {
        this.userId = userId;
        this.rollNumber = rollNumber;
        this.program = program;
        this.year = year;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(int rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "Student{" +
                "userId='" + userId + '\'' +
                ", rollNumber=" + rollNumber +
                ", program='" + program + '\'' +
                ", year=" + year +
                '}';
    }
}

