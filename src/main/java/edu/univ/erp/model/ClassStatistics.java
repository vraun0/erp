package edu.univ.erp.model;

import java.util.Map;

public class ClassStatistics {
    private final double averageScore;
    private final int minScore;
    private final int maxScore;
    private final double stdDev;
    private final int studentCount;
    private final Map<String, Integer> gradeDistribution; // A, B, C, D, F counts

    public ClassStatistics(double averageScore, int minScore, int maxScore, double stdDev, int studentCount,
            Map<String, Integer> gradeDistribution) {
        this.averageScore = averageScore;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.stdDev = stdDev;
        this.studentCount = studentCount;
        this.gradeDistribution = gradeDistribution;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public double getStdDev() {
        return stdDev;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public Map<String, Integer> getGradeDistribution() {
        return gradeDistribution;
    }
}
