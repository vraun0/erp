package edu.univ.erp.model;

/**
 * Instructor record mapped to ERPDB.instructors
 */
public class Instructor {
    private int userId; // references AuthDB.users_auth.user_id
    private String department;

    public Instructor() {
    }

    public Instructor(int userId, String department) {
        this.userId = userId;
        this.department = department;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return userId + (department != null && !department.isBlank() ? " (" + department + ")" : "");
    }
}
