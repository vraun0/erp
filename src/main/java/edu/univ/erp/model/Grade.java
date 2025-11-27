package edu.univ.erp.model;

/**
 * Grade record mapped to ERPDB.grades
 */
public class Grade {
    private int enrollmentId;  // references enrollments.enrollment_id
    private int component;     // e.g., 1 = midterm, 2 = final
    private int score;         // score for component
    private Integer finalScore; // optional aggregate/final grade

    public Grade() {
    }

    public Grade(int enrollmentId, int component, int score, Integer finalScore) {
        this.enrollmentId = enrollmentId;
        this.component = component;
        this.score = score;
        this.finalScore = finalScore;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public int getComponent() {
        return component;
    }

    public void setComponent(int component) {
        this.component = component;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Integer getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(Integer finalScore) {
        this.finalScore = finalScore;
    }

    @Override
    public String toString() {
        return "Grade{" +
                "enrollmentId=" + enrollmentId +
                ", component=" + component +
                ", score=" + score +
                ", finalScore=" + finalScore +
                '}';
    }
}


