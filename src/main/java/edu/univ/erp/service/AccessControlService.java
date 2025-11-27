package edu.univ.erp.service;

import edu.univ.erp.dao.EnrollmentDao;
import edu.univ.erp.dao.SectionDao;
import edu.univ.erp.dao.UserDao;
import edu.univ.erp.model.Enrollment;
import edu.univ.erp.model.Section;
import edu.univ.erp.model.User;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Service for handling access control and permission checks.
 * Centralizes logic for who can access/edit what resources.
 */
public class AccessControlService {
    private final SectionDao sectionDao;
    private final EnrollmentDao enrollmentDao;
    private final UserDao userDao;

    public AccessControlService() {
        this.sectionDao = new SectionDao();
        this.enrollmentDao = new EnrollmentDao();
        this.userDao = new UserDao();
    }

    /**
     * Checks if a user is an admin.
     */
    public boolean isAdmin(int userId) throws SQLException {
        User user = userDao.findById(userId);
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    /**
     * Checks if an instructor is allowed to edit a specific section.
     * Allowed if: User is the assigned instructor for the section.
     */
    public boolean canEditSection(int userId, int sectionId) throws SQLException {
        Optional<Section> sectionOpt = sectionDao.findById(sectionId);
        if (sectionOpt.isEmpty()) {
            return false; // Section doesn't exist, so can't edit
        }
        Section section = sectionOpt.get();
        // Check if instructorId matches userId
        // Note: section.getInstructorId() returns Integer
        return section.getInstructorId() != null && section.getInstructorId() == userId;
    }

    /**
     * Checks if a user is allowed to view/edit data for a specific student.
     * Allowed if: User IS the student.
     * (Instructors might have read access via other methods, but this is for direct
     * student data ownership)
     */
    public boolean canAccessStudentData(int requesterId, int studentId) {
        return requesterId == studentId;
    }

    /**
     * Checks if a student is allowed to modify an enrollment (e.g. drop).
     * Allowed if: Enrollment belongs to the student.
     */
    public boolean canModifyEnrollment(int studentId, int enrollmentId) throws SQLException {
        Optional<Enrollment> enrollmentOpt = enrollmentDao.findById(enrollmentId);
        if (enrollmentOpt.isEmpty()) {
            return false;
        }
        return enrollmentOpt.get().getStudentId() == studentId;
    }
}
