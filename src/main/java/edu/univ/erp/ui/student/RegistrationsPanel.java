package edu.univ.erp.ui.student;

import edu.univ.erp.model.view.EnrollmentView;
import edu.univ.erp.service.ServiceException;
import edu.univ.erp.service.ServiceException;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class RegistrationsPanel extends AbstractStudentPanel {
    private final JPanel cardsContainer;
    private final JLabel statusLabel;
    private final JScrollPane scrollPane;

    public RegistrationsPanel(StudentService studentService, String studentId) {
        super(studentService, studentId);
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND_DARK);

        // Header
        JPanel headerPanel = new JPanel(new MigLayout("fillx, insets 24 28 20 28", "[grow][]", "[]"));
        headerPanel.setOpaque(false);
        JLabel headerLabel = UIStyle.createHeading("My Registrations", 2);
        headerPanel.add(headerLabel, "growx");

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIStyle.FONT_BODY);
        statusLabel.setForeground(UIStyle.TEXT_SECONDARY);
        headerPanel.add(statusLabel, "align right, gapleft 16");
        add(headerPanel, BorderLayout.NORTH);

        // Cards container
        cardsContainer = new JPanel();
        cardsContainer.setLayout(new MigLayout("fillx, wrap, insets 24", "[grow]", "[]24[]"));
        cardsContainer.setOpaque(false);
        cardsContainer.setBorder(new EmptyBorder(0, 28, 28, 28));

        scrollPane = new JScrollPane(cardsContainer);
        UIStyle.styleScrollPane(scrollPane);
        scrollPane.getViewport().setBackground(UIStyle.BACKGROUND_DARK);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createRegistrationCard(EnrollmentView view) {
        JPanel card = UIStyle.createCard();
        card.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]12[]8[]6[]6[]6[]16[]"));
        card.setPreferredSize(new Dimension(0, 260));

        // Course title
        JLabel courseLabel = new JLabel("<html>" + view.getCourseDisplay() + "</html>");
        courseLabel.setFont(UIStyle.FONT_H3);
        courseLabel.setForeground(UIStyle.ACCENT_BLUE);
        courseLabel.setVerticalAlignment(SwingConstants.TOP);
        card.add(courseLabel, "growx, wrap");

        // Status badge
        JLabel statusBadge = createStatusBadge(view.getStatus());
        card.add(statusBadge, "growx 0, wrap");

        // Details
        addDetailRow(card, "📖 Section", "#" + view.getSectionId());
        addDetailRow(card, "👨‍🏫 Instructor", view.getInstructorName());
        addDetailRow(card, "📅 Schedule", view.getDayTime());
        addDetailRow(card, "🏢 Room", view.getRoom());
        if (view.getDropDeadline() != null) {
            addDetailRow(card, "⏰ Drop Deadline", view.getDropDeadline().toString());
        }

        // Drop button (only for enrolled)
        if ("ENROLLED".equalsIgnoreCase(view.getStatus())) {
            JButton dropBtn = UIStyle.createSecondaryButton("Drop Section");
            dropBtn.setPreferredSize(new Dimension(0, 46));
            dropBtn.addActionListener(e -> dropSection(view.getSectionId()));
            card.add(dropBtn, "growx");
        }

        return card;
    }

    private JLabel createStatusBadge(String status) {
        JLabel badge = new JLabel(status);
        badge.setFont(UIStyle.FONT_BODY);
        badge.setBorder(new EmptyBorder(6, 14, 6, 14));
        badge.setOpaque(true);

        if ("ENROLLED".equalsIgnoreCase(status)) {
            badge.setForeground(UIStyle.SUCCESS_GREEN);
            badge.setBackground(new Color(UIStyle.SUCCESS_GREEN.getRed(),
                    UIStyle.SUCCESS_GREEN.getGreen(), UIStyle.SUCCESS_GREEN.getBlue(), 40));
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
        labelComp.setFont(UIStyle.FONT_BODY);
        labelComp.setForeground(UIStyle.TEXT_SECONDARY);
        row.add(labelComp, "growx 0, width 140:140:");

        JLabel valueComp = new JLabel("<html>" + value + "</html>");
        valueComp.setFont(UIStyle.FONT_BODY_BOLD);
        valueComp.setForeground(UIStyle.TEXT_PRIMARY);
        valueComp.setHorizontalAlignment(SwingConstants.RIGHT);
        valueComp.setVerticalAlignment(SwingConstants.CENTER);
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
                studentService.dropSection(Integer.parseInt(studentId), sectionId);
                JOptionPane.showMessageDialog(this, "Section dropped successfully.",
                        "Drop Complete", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (SQLException | ServiceException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Drop Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void refreshData() {
        try {
            List<EnrollmentView> enrollments = studentService.getCurrentRegistrations(Integer.parseInt(studentId));

            cardsContainer.removeAll();

            for (EnrollmentView view : enrollments) {
                cardsContainer.add(createRegistrationCard(view), "growx, wrap");
            }

            statusLabel.setText(enrollments.isEmpty() ? "No registrations"
                    : enrollments.size() + " registration" + (enrollments.size() != 1 ? "s" : ""));

            cardsContainer.revalidate();
            cardsContainer.repaint();
            scrollPane.revalidate();

        } catch (SQLException | ServiceException ex) {
            statusLabel.setText("Failed to load: " + ex.getMessage());
            statusLabel.setForeground(UIStyle.ERROR_RED);
        }
    }
}
