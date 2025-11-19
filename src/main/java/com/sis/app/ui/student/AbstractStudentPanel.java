package com.sis.app.ui.student;

import com.sis.app.service.StudentService;

import javax.swing.JPanel;

abstract class AbstractStudentPanel extends JPanel {
    protected final StudentService studentService;
    protected final String studentId;

    protected AbstractStudentPanel(StudentService studentService, String studentId) {
        this.studentService = studentService;
        this.studentId = studentId;
    }

    abstract void refreshData();
}

