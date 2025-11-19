package com.sis.app.ui.student;

import com.sis.app.model.view.EnrollmentView;
import com.sis.app.service.StudentService;
import com.sis.app.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

class TimetablePanel extends AbstractStudentPanel {
    private final JPanel cardsContainer;
    private final JLabel statusLabel;
    private final JScrollPane scrollPane;

    TimetablePanel(StudentService studentService, String studentId) {
        super(studentService, studentId);
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND_DARK);
        
        // Header
        JPanel headerPanel = new JPanel(new MigLayout("fillx, insets 20 24 16 24", "[grow][]", "[]"));
        headerPanel.setOpaque(false);
        JLabel headerLabel = UIStyle.createHeading("My Timetable", 2);
        headerPanel.add(headerLabel, "growx");
        
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIStyle.FONT_SMALL);
        statusLabel.setForeground(UIStyle.TEXT_SECONDARY);
        headerPanel.add(statusLabel, "align right");
        add(headerPanel, BorderLayout.NORTH);

        // Cards container with grid
        cardsContainer = new JPanel();
        cardsContainer.setLayout(new MigLayout("fillx, wrap, insets 20", 
            "[grow 0][grow 0]", "[]20[]"));
        cardsContainer.setOpaque(false);
        cardsContainer.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        scrollPane = new JScrollPane(cardsContainer);
        UIStyle.styleScrollPane(scrollPane);
        scrollPane.getViewport().setBackground(UIStyle.BACKGROUND_DARK);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createTimetableCard(EnrollmentView view) {
        JPanel card = UIStyle.createCard();
        card.setLayout(new MigLayout("fillx, wrap, insets 20", "[grow]", "[]8[]4[]4[]4[]"));
        card.setPreferredSize(new Dimension(420, 200));
        card.setMinimumSize(new Dimension(380, 180));
        
        // Course title
        JLabel courseLabel = new JLabel(view.getCourseDisplay());
        courseLabel.setFont(UIStyle.FONT_H3);
        courseLabel.setForeground(UIStyle.ACCENT_BLUE);
        card.add(courseLabel, "growx, wrap");
        
        // Section badge
        JLabel sectionBadge = new JLabel("Section #" + view.getSectionId());
        sectionBadge.setFont(UIStyle.FONT_SMALL);
        sectionBadge.setForeground(UIStyle.ACCENT_BLUE_HOVER);
        sectionBadge.setBorder(new EmptyBorder(4, 12, 4, 12));
        sectionBadge.setOpaque(true);
        sectionBadge.setBackground(new Color(UIStyle.DEEP_GRAY.getRed(), 
            UIStyle.DEEP_GRAY.getGreen(), UIStyle.DEEP_GRAY.getBlue(), 100));
        card.add(sectionBadge, "growx 0, wrap");
        
        // Schedule details
        addDetailRow(card, "📅 Time", view.getDayTime());
        addDetailRow(card, "🏢 Location", view.getRoom());
        
        return card;
    }
    
    private void addDetailRow(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new MigLayout("fillx, insets 0", "[][grow]", "[]"));
        row.setOpaque(false);
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(UIStyle.FONT_BODY);
        labelComp.setForeground(UIStyle.TEXT_SECONDARY);
        row.add(labelComp, "growx 0");
        
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(UIStyle.FONT_BODY_BOLD);
        valueComp.setForeground(UIStyle.TEXT_PRIMARY);
        valueComp.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(valueComp, "growx");
        
        parent.add(row, "growx, wrap");
    }

    @Override
    void refreshData() {
        try {
            List<EnrollmentView> enrollments = studentService.getCurrentRegistrations(studentId);
            
            cardsContainer.removeAll();
            
            int enrolledCount = 0;
            for (EnrollmentView view : enrollments) {
                if ("ENROLLED".equalsIgnoreCase(view.getStatus())) {
                    cardsContainer.add(createTimetableCard(view), "growx");
                    enrolledCount++;
                }
            }
            
            statusLabel.setText(enrolledCount == 0 ? "No enrolled courses" : 
                enrolledCount + " course" + (enrolledCount != 1 ? "s" : "") + " scheduled");
            
            cardsContainer.revalidate();
            cardsContainer.repaint();
            scrollPane.revalidate();
            
        } catch (SQLException ex) {
            statusLabel.setText("Failed to load timetable: " + ex.getMessage());
            statusLabel.setForeground(UIStyle.ERROR_RED);
        }
    }
}

