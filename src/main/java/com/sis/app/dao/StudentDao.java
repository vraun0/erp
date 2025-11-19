package com.sis.app.dao;

import com.sis.app.config.DatabaseManager;
import com.sis.app.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDao {
    private static final String SELECT_BY_USER = "SELECT user_id, roll_no, program, year FROM students WHERE user_id = ?";
    private static final String INSERT_STUDENT = "INSERT INTO students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
    private static final String SELECT_ALL = "SELECT user_id, roll_no, program, year FROM students ORDER BY roll_no";

    private final DatabaseManager databaseManager;

    public StudentDao() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    public Optional<Student> findByUserId(String userId) throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USER)) {

            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Student student = new Student();
                    student.setUserId(rs.getString("user_id"));
                    student.setRollNumber(rs.getInt("roll_no"));
                    student.setProgram(rs.getString("program"));
                    student.setYear(rs.getInt("year"));
                    return Optional.of(student);
                }
            }
        }
        return Optional.empty();
    }

    public void insertStudent(Student student) throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_STUDENT)) {
            stmt.setString(1, student.getUserId());
            stmt.setInt(2, student.getRollNumber());
            stmt.setString(3, student.getProgram());
            stmt.setInt(4, student.getYear());
            stmt.executeUpdate();
        }
    }

    public List<Student> findAll() throws SQLException {
        List<Student> students = new ArrayList<>();
        try (Connection conn = databaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Student student = new Student();
                student.setUserId(rs.getString("user_id"));
                student.setRollNumber(rs.getInt("roll_no"));
                student.setProgram(rs.getString("program"));
                student.setYear(rs.getInt("year"));
                students.add(student);
            }
        }
        return students;
    }
}
