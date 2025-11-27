package edu.univ.erp.dao;

import edu.univ.erp.config.DatabaseManager;
import edu.univ.erp.model.Section;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SectionDao {
    private static final String SELECT_BY_ID = "SELECT section_id, course_id, instructor_id, day_time, room, capacity, semester, year, drop_deadline FROM sections WHERE section_id = ?";
    private static final String SELECT_ALL = "SELECT section_id, course_id, instructor_id, day_time, room, capacity, semester, year, drop_deadline FROM sections";
    private static final String INSERT_SECTION = "INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year, drop_deadline) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_INSTRUCTOR = "UPDATE sections SET instructor_id = ? WHERE section_id = ?";

    private final DatabaseManager databaseManager;

    public SectionDao() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    public Optional<Section> findById(int sectionId) throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapSection(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Section> findAll() throws SQLException {
        List<Section> sections = new ArrayList<>();
        try (Connection conn = databaseManager.getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_ALL)) {
            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    sections.add(mapSection(rs));
                }
            }
        }
        return sections;
    }

    public int insertSection(Section section) throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(INSERT_SECTION,
                        PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, section.getCourseId());
            if (section.getInstructorId() != null) {
                stmt.setInt(2, section.getInstructorId());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            stmt.setString(3, section.getDayTime());
            stmt.setString(4, section.getRoom());
            stmt.setInt(5, section.getCapacity());
            stmt.setInt(6, section.getSemester());
            stmt.setInt(7, section.getYear());
            if (section.getDropDeadline() != null) {
                stmt.setDate(8, java.sql.Date.valueOf(section.getDropDeadline()));
            } else {
                stmt.setNull(8, java.sql.Types.DATE);
            }
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create section");
    }

    public void updateInstructor(int sectionId, Integer instructorId) throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(UPDATE_INSTRUCTOR)) {
            if (instructorId != null) {
                stmt.setInt(1, instructorId);
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setInt(2, sectionId);
            stmt.executeUpdate();
        }
    }

    public void delete(int sectionId) throws SQLException {
        String sql = "DELETE FROM sections WHERE section_id = ?";
        try (Connection conn = databaseManager.getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            stmt.executeUpdate();
        }
    }

    private Section mapSection(ResultSet rs) throws SQLException {
        Section section = new Section();
        section.setSectionId(rs.getInt("section_id"));
        section.setCourseId(rs.getString("course_id"));
        Integer instructorId = rs.getObject("instructor_id", Integer.class);
        section.setInstructorId(instructorId);
        section.setDayTime(rs.getString("day_time"));
        section.setRoom(rs.getString("room"));
        section.setCapacity(rs.getInt("capacity"));
        section.setSemester(rs.getInt("semester"));
        section.setYear(rs.getInt("year"));
        java.sql.Date dropDeadline = rs.getDate("drop_deadline");
        if (dropDeadline != null) {
            section.setDropDeadline(dropDeadline.toLocalDate());
        }
        return section;
    }
}
