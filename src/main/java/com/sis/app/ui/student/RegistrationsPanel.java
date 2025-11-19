package com.sis.app.ui.student;

import com.sis.app.model.view.EnrollmentView;
import com.sis.app.service.ServiceException;
import com.sis.app.service.StudentService;
import com.sis.app.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

class RegistrationsPanel extends AbstractStudentPanel {
    private final JPanel cardsContainer;
    private final JLabel statusLabel;
    private final JScrollPane scrollPane;

    RegistrationsPanel(StudentService studentService, String studentId) {
        super(studentService, studentId);
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND_DARK);
        
        // Header
        JPanel headerPanel = new JPanel(new MigLayout("fillx, insets 20 24 16 24", "[grow][]", "[]"));
        headerPanel.setOpaque(false);
        JLabel headerLabel = UIStyle.createHeading("My Registrations", 2);
        headerPanel.add(headerLabel, "growx");
        
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIStyle.FONT_SMALL);
        statusLabel.setForeground(UIStyle.TEXT_SECONDARY);
        headerPanel.add(statusLabel, "align right");
        add(headerPanel, BorderLayout.NORTH);

        // Cards container
        cardsContainer = new JPanel();
        cardsContainer.setLayout(new MigLayout("fillx, wrap, insets 20", "[grow]", "[]20[]"));
        cardsContainer.setOpaque(false);
        cardsContainer.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        scrollPane = new JScrollPane(cardsContainer);
        UIStyle.styleScrollPane(scrollPane);
        scrollPane.getViewport().setBackground(UIStyle.BACKGROUND_DARK);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createRegistrationCard(EnrollmentView view) {
        JPanel card = UIStyle.createCard();
        card.setLayout(new MigLayout("fillx, wrap, insets 20", "[grow]", "[]8[]4[]4[]4[]12[]"));
        card.setPreferredSize(new Dimension(0, 220));
        
        // Course title
        JLabel courseLabel = new JLabel(view.getCourseDisplay());
        courseLabel.setFont(UIStyle.FONT_H3);
        courseLabel.setForeground(UIStyle.ACCENT_BLUE);
        card.add(courseLabel, "growx, wrap");
        
        // Status badge
        JLabel statusBadge = createStatusBadge(view.getStatus());
        card.add(statusBadge, "growx 0, wrap");
        
        // Details
        addDetailRow(card, "📖 Section", "#" + view.getSectionId());
        addDetailRow(card, "📅 Schedule", view.getDayTime());
        addDetailRow(card, "🏢 Room", view.getRoom());
        
        // Drop button (only for enrolled)
        if ("ENROLLED".equalsIgnoreCase(view.getStatus())) {
            JButton dropBtn = UIStyle.createSecondaryButton("Drop Section");
            dropBtn.setPreferredSize(new Dimension(0, 42));
            dropBtn.addActionListener(e -> dropSection(view.getSectionId()));
            card.add(dropBtn, "growx");
        }
        
        return card;
    }
    
    private JLabel createStatusBadge(String status) {
        JLabel badge = new JLabel(status);
        badge.setFont(UIStyle.FONT_SMALL);
        badge.setBorder(new EmptyBorder(4, 12, 4, 12));
        badge.setOpaque(true);
        
        if ("ENROLLED".equalsIgnoreCase(status)) {
            badge.setForeground(UIStyle.SUCCESS_GREEN);
            badge.setBackground(new Color(UIStyle.SUCCESS_GREEN.getRed(), 
                UIStyle.SUCCESS_GREEN.getGreen(), UIStyle.SUCCESS_GREEN.getBlue(), 30));
        } else {
            badge.setForeground(UIStyle.TEXT_SECONDARY);
            badge.setBackground(UIStyle.BORDER_COLOR);
        }
        
        return badge;
    }
    
    private void addDetailRow(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new MigLayout("fillx, insets 0", "[][grow]", "[]"));
        row.setOpaque(false);
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(UIStyle.FONT_SMALL);
        labelComp.setForeground(UIStyle.TEXT_SECONDARY);
        row.add(labelComp, "growx 0");
        
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(UIStyle.FONT_BODY);
        valueComp.setForeground(UIStyle.TEXT_PRIMARY);
        valueComp.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(valueComp, "growx");
        
        parent.add(row, "growx, wrap");
    }
    
    private void dropSection(int sectionId) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to drop this section?",
            "Confirm Drop",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                studentService.dropSection(studentId, sectionId);
                JOptionPane.showMessageDialog(this, "Section dropped successfully.",
                    "Drop Complete", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (ServiceException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), 
                    "Drop Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Drop Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    void refreshData() {
        try {
            List<EnrollmentView> enrollments = studentService.getCurrentRegistrations(studentId);
            
            cardsContainer.removeAll();
            
            for (EnrollmentView view : enrollments) {
                cardsContainer.add(createRegistrationCard(view), "growx, wrap");
            }
            
            statusLabel.setText(enrollments.isEmpty() ? "No registrations" : 
                enrollments.size() + " registration" + (enrollments.size() != 1 ? "s" : ""));
            
            cardsContainer.revalidate();
            cardsContainer.repaint();
            scrollPane.revalidate();
            
        } catch (SQLException ex) {
            statusLabel.setText("Failed to load: " + ex.getMessage());
            statusLabel.setForeground(UIStyle.ERROR_RED);
        }
    }
}

