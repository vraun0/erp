package edu.univ.erp.ui.student;

import edu.univ.erp.model.view.EnrollmentView;
import edu.univ.erp.service.ServiceException;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TimetablePanel extends AbstractStudentPanel {
    private final JTabbedPane tabbedPane;
    private final JLabel statusLabel;

    public TimetablePanel(StudentService studentService, String studentId) {
        super(studentService, studentId);
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND_DARK);

        // Header
        JPanel headerPanel = new JPanel(new MigLayout("fillx, insets 24 28 10 28", "[grow][]", "[]"));
        headerPanel.setOpaque(false);
        JLabel headerLabel = UIStyle.createHeading("Weekly Schedule", 2);
        headerPanel.add(headerLabel, "growx");

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIStyle.FONT_BODY);
        statusLabel.setForeground(UIStyle.TEXT_SECONDARY);
        headerPanel.add(statusLabel, "align right, gapleft 16");
        add(headerPanel, BorderLayout.NORTH);

        // Tabbed Pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIStyle.FONT_BODY_BOLD);
        tabbedPane.setBorder(new EmptyBorder(0, 20, 20, 20));

        add(tabbedPane, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        try {
            List<EnrollmentView> enrollments = studentService.getCurrentRegistrations(Integer.parseInt(studentId));

            // Filter only enrolled courses
            List<EnrollmentView> activeEnrollments = enrollments.stream()
                    .filter(e -> "ENROLLED".equalsIgnoreCase(e.getStatus()))
                    .collect(Collectors.toList());

            tabbedPane.removeAll();

            String[] days = { "Mon", "Tue", "Wed", "Thu", "Fri" };
            String[] fullDays = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday" };

            for (int i = 0; i < days.length; i++) {
                String dayShort = days[i];
                String dayFull = fullDays[i];

                List<EnrollmentView> dayClasses = activeEnrollments.stream()
                        .filter(e -> e.getDayTime() != null && e.getDayTime().startsWith(dayShort))
                        .sorted(Comparator.comparing(EnrollmentView::getDayTime)) // Simple string sort for now
                        .collect(Collectors.toList());

                tabbedPane.addTab(dayFull, createDayPanel(dayClasses));
            }

            statusLabel.setText(activeEnrollments.isEmpty() ? "No classes scheduled"
                    : activeEnrollments.size() + " active courses");

        } catch (SQLException | ServiceException ex) {
            statusLabel.setText("Error: " + ex.getMessage());
            statusLabel.setForeground(UIStyle.ERROR_RED);
        }
    }

    private JScrollPane createDayPanel(List<EnrollmentView> classes) {
        JPanel container = new JPanel(new MigLayout("fillx, wrap, insets 20", "[grow]", "[]12[]"));
        container.setOpaque(false);

        if (classes.isEmpty()) {
            JPanel emptyState = UIStyle.createCard();
            emptyState.setLayout(new MigLayout("fill, insets 40", "[center]", "[]"));
            JLabel emptyLabel = new JLabel("No classes scheduled for this day");
            emptyLabel.setFont(UIStyle.FONT_BODY);
            emptyLabel.setForeground(UIStyle.TEXT_MUTED);
            emptyState.add(emptyLabel);
            container.add(emptyState, "growx");
        } else {
            for (EnrollmentView view : classes) {
                container.add(createTimelineItem(view), "growx");
            }
        }

        JScrollPane scroll = new JScrollPane(container);
        UIStyle.styleScrollPane(scroll);
        scroll.getViewport().setBackground(UIStyle.BACKGROUND_DARK);
        scroll.setBorder(null);
        return scroll;
    }

    private JPanel createTimelineItem(EnrollmentView view) {
        JPanel shadowWrapper = UIStyle.createDropShadowPanel();

        // Create content panel with proper layout
        JPanel contentPanel = new JPanel(new MigLayout("fillx, insets 16, gap 20", "[120px][grow]", "[]"));
        contentPanel.setOpaque(false);

        // Time Column
        JPanel timePanel = new JPanel(new MigLayout("insets 0", "[]", "[]4[]"));
        timePanel.setOpaque(false);

        // Parse time from "Mon 10:00 AM" -> "10:00 AM"
        String timeStr = view.getDayTime() != null ? view.getDayTime().replaceFirst("^[A-Za-z]+\\s+", "") : "TBA";

        JLabel timeLabel = new JLabel(timeStr);
        timeLabel.setFont(UIStyle.FONT_H3);
        timeLabel.setForeground(UIStyle.ACCENT_BLUE);
        timePanel.add(timeLabel, "wrap");

        JLabel durationLabel = new JLabel("1 hr");
        durationLabel.setFont(UIStyle.FONT_SMALL);
        durationLabel.setForeground(UIStyle.TEXT_MUTED);
        timePanel.add(durationLabel);

        contentPanel.add(timePanel, "aligny top");

        // Details Column
        JPanel detailsPanel = new JPanel(new MigLayout("fillx, insets 0", "[grow]", "[]4[]8[]"));
        detailsPanel.setOpaque(false);

        JLabel courseLabel = new JLabel("<html>" + view.getCourseDisplay() + "</html>");
        courseLabel.setFont(UIStyle.FONT_H4);
        courseLabel.setForeground(UIStyle.TEXT_PRIMARY);
        detailsPanel.add(courseLabel, "wrap, growx");

        JLabel roomLabel = new JLabel("📍 " + view.getRoom());
        roomLabel.setFont(UIStyle.FONT_BODY);
        roomLabel.setForeground(UIStyle.TEXT_SECONDARY);
        detailsPanel.add(roomLabel, "wrap, growx");

        JLabel sectionLabel = new JLabel("Section " + view.getSectionId());
        sectionLabel.setFont(UIStyle.FONT_SMALL);
        sectionLabel.setForeground(UIStyle.TEXT_MUTED);
        sectionLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, UIStyle.BORDER_COLOR),
                new EmptyBorder(2, 6, 2, 6)));
        detailsPanel.add(sectionLabel);

        contentPanel.add(detailsPanel, "growx");

        // Add content panel to shadow wrapper
        shadowWrapper.setLayout(new BorderLayout());
        shadowWrapper.add(contentPanel, BorderLayout.CENTER);

        return shadowWrapper;
    }
}
