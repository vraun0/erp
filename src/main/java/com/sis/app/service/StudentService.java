package com.sis.app.service;

import com.sis.app.dao.CourseDao;
import com.sis.app.dao.EnrollmentDao;
import com.sis.app.dao.GradeDao;
import com.sis.app.dao.SectionDao;
import com.sis.app.dao.SettingsDao;
import com.sis.app.model.Course;
import com.sis.app.model.Enrollment;
import com.sis.app.model.Grade;
import com.sis.app.model.Section;
import com.sis.app.model.view.CourseSectionView;
import com.sis.app.model.view.EnrollmentView;
import com.sis.app.model.view.GradeView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentService {
    private final CourseDao courseDao;
    private final SectionDao sectionDao;
    private final EnrollmentDao enrollmentDao;
    private final GradeDao gradeDao;
    private final SettingsDao settingsDao;

    public StudentService() {
        this.courseDao = new CourseDao();
        this.sectionDao = new SectionDao();
        this.enrollmentDao = new EnrollmentDao();
        this.gradeDao = new GradeDao();
        this.settingsDao = new SettingsDao();
    }

    public List<CourseSectionView> getCourseCatalog() throws SQLException {
        List<CourseSectionView> catalog = new ArrayList<>();
        List<Section> sections = sectionDao.findAll();
        
        for (Section section : sections) {
            Optional<Course> courseOpt = courseDao.findByCode(section.getCourseId());
            if (courseOpt.isEmpty()) {
                continue;
            }
            Course course = courseOpt.get();
            
            int seatsTaken = enrollmentDao.countActiveEnrollmentsForSection(section.getSectionId());
            
            catalog.add(new CourseSectionView(
                    course.getCode(),
                    course.getTitle(),
                    course.getCredits(),
                    section.getSectionId(),
                    section.getDayTime(),
                    section.getRoom(),
                    section.getInstructorId(),
                    section.getCapacity(),
                    seatsTaken,
                    section.getSemester(),
                    section.getYear()
            ));
        }
        
        return catalog;
    }

    public void registerForSection(String studentId, int sectionId) throws SQLException, ServiceException {
        ensureSystemWritable();
        
        Optional<Section> sectionOpt = sectionDao.findById(sectionId);
        if (sectionOpt.isEmpty()) {
            throw new ServiceException("Section not found.");
        }
        Section section = sectionOpt.get();
        
        Optional<Enrollment> existingOpt = enrollmentDao.findActiveEnrollment(studentId, sectionId);
        if (existingOpt.isPresent()) {
            throw new ServiceException("You are already enrolled in this section.");
        }
        
        int seatsTaken = enrollmentDao.countActiveEnrollmentsForSection(sectionId);
        if (seatsTaken >= section.getCapacity()) {
            throw new ServiceException("Section is full. No seats available.");
        }
        
        enrollmentDao.insertEnrollment(studentId, sectionId);
    }

    public List<GradeView> getGrades(String studentId) throws SQLException {
        List<GradeView> gradeViews = new ArrayList<>();
        List<Grade> grades = gradeDao.findByStudent(studentId);
        
        for (Grade grade : grades) {
            Optional<Enrollment> enrollmentOpt = enrollmentDao.findById(grade.getEnrollmentId());
            if (enrollmentOpt.isEmpty()) {
                continue;
            }
            
            Enrollment enrollment = enrollmentOpt.get();
            
            Optional<Section> sectionOpt = sectionDao.findById(enrollment.getSectionId());
            if (sectionOpt.isEmpty()) {
                continue;
            }
            
            Optional<Course> courseOpt = courseDao.findByCode(sectionOpt.get().getCourseId());
            if (courseOpt.isEmpty()) {
                continue;
            }
            
            Course course = courseOpt.get();
            
            Integer finalScore = grade.getFinalScore();
            
            gradeViews.add(new GradeView(
                    course.getCode(),
                    course.getTitle(),
                    enrollment.getSectionId(),
                    grade.getComponent(),
                    grade.getScore(),
                    finalScore
            ));
        }
        
        return gradeViews;
    }

    public List<EnrollmentView> getCurrentRegistrations(String studentId) throws SQLException {
        List<EnrollmentView> views = new ArrayList<>();
        List<Enrollment> enrollments = enrollmentDao.findByStudent(studentId);
        
        for (Enrollment enrollment : enrollments) {
            Optional<Section> sectionOpt = sectionDao.findById(enrollment.getSectionId());
            if (sectionOpt.isEmpty()) {
                continue;
            }
            
            Optional<Course> courseOpt = courseDao.findByCode(sectionOpt.get().getCourseId());
            if (courseOpt.isEmpty()) {
                continue;
            }
            
            Course course = courseOpt.get();
            Section section = sectionOpt.get();
            
            views.add(new EnrollmentView(
                    enrollment.getEnrollmentId(),
                    enrollment.getSectionId(),
                    course.getCode(),
                    course.getTitle(),
                    section.getDayTime(),
                    section.getRoom(),
                    enrollment.getStatus()
            ));
        }
        
        return views;
    }

    public void dropSection(String studentId, int sectionId) throws SQLException, ServiceException {
        ensureSystemWritable();
        
        Optional<Enrollment> enrollmentOpt = enrollmentDao.findActiveEnrollment(studentId, sectionId);
        if (enrollmentOpt.isEmpty()) {
            throw new ServiceException("You are not enrolled in this section.");
        }
        
        Enrollment enrollment = enrollmentOpt.get();
        enrollmentDao.updateStatus(enrollment.getEnrollmentId(), "DROPPED");
    }

    private void ensureSystemWritable() throws SQLException, ServiceException {
        if (settingsDao.isMaintenanceMode()) {
            throw new ServiceException("System is currently in maintenance mode. Please try again later.");
        }
    }
}

