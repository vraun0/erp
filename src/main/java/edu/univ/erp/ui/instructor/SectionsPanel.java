package edu.univ.erp.ui.instructor;

import edu.univ.erp.api.instructor.InstructorAPI;
import edu.univ.erp.api.types.CourseRow;
import edu.univ.erp.api.types.SectionRow;
import edu.univ.erp.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class SectionsPanel extends JPanel {
    private final InstructorAPI instructorAPI;
    private final String instructorId;
    private final JPanel cardsContainer;
    private final JLabel statusLabel;
    private final JScrollPane scrollPane;

    public SectionsPanel(InstructorAPI instructorAPI, String instructorId) {
        this.instructorAPI = instructorAPI;
        this.instructorId = instructorId;
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND_DARK);

        // Header
        JPanel headerPanel = new JPanel(new MigLayout("fillx, insets 24 28 20 28", "[grow][]", "[]"));
        headerPanel.setOpaque(false);
        JLabel headerLabel = UIStyle.createHeading("My Assigned Sections", 2);
        headerPanel.add(headerLabel, "growx");

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIStyle.FONT_BODY);
        statusLabel.setForeground(UIStyle.TEXT_SECONDARY);
        headerPanel.add(statusLabel, "align right, gapleft 16");
        add(headerPanel, BorderLayout.NORTH);

        // Cards container
        cardsContainer = new JPanel();
        cardsContainer.setLayout(new MigLayout("fillx, wrap, insets 24",
                "[grow 0][grow 0]", "[]24[]"));
        cardsContainer.setOpaque(false);
        cardsContainer.setBorder(new EmptyBorder(0, 28, 28, 28));

        scrollPane = new JScrollPane(cardsContainer);
        UIStyle.styleScrollPane(scrollPane);
        scrollPane.getViewport().setBackground(UIStyle.BACKGROUND_DARK);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createSectionCard(SectionRow section, CourseRow course) {
        JPanel card = UIStyle.createCard();
        card.setLayout(new MigLayout("fillx, wrap, insets 0", "[grow]", "[]12[]8[]6[]6[]6[]6[]6[]"));
        card.setPreferredSize(new Dimension(480, 320));
        card.setMinimumSize(new Dimension(440, 300));

        // Section ID badge
        JLabel sectionBadge = new JLabel("Section #" + section.sectionId());
        sectionBadge.setFont(UIStyle.FONT_BODY_BOLD);
        sectionBadge.setForeground(UIStyle.ACCENT_BLUE);
        sectionBadge.setBorder(new EmptyBorder(6, 14, 6, 14));
        sectionBadge.setOpaque(true);
        sectionBadge.setBackground(new Color(UIStyle.DEEP_GRAY.getRed(),
                UIStyle.DEEP_GRAY.getGreen(), UIStyle.DEEP_GRAY.getBlue(), 150));
        card.add(sectionBadge, "growx 0, wrap");

        // Course title
        String courseDisplay = course != null ? course.code() + " - " + course.title() : section.courseCode();
        JLabel courseLabel = new JLabel("<html>" + courseDisplay + "</html>");
        courseLabel.setFont(UIStyle.FONT_H3);
        courseLabel.setForeground(UIStyle.TEXT_PRIMARY);
        courseLabel.setVerticalAlignment(SwingConstants.TOP);
        card.add(courseLabel, "growx, wrap");

        // Details
        addDetailRow(card, "📚 Credits", course != null ? String.valueOf(course.credits()) : "-");
        addDetailRow(card, "📅 Schedule", section.dayTime());
        addDetailRow(card, "🏢 Room", section.room());
        addDetailRow(card, "👥 Capacity", String.valueOf(section.capacity()));
        // SectionRow doesn't have semester/year, so we skip or add them to SectionRow.
        // For now, let's skip or hardcode if needed.
        // Actually SectionRow definition I saw earlier:
        // record SectionRow(int sectionId,String courseCode,String dayTime,String
        // room,int capacity,int enrolled,String instructorName)
        // It misses semester/year. I should probably add them to SectionRow if
        // critical.
        // But for now I'll just remove these rows to avoid compilation error.
        // addDetailRow(card, "📆 Semester", String.valueOf(section.getSemester()));
        // addDetailRow(card, "📅 Year", String.valueOf(section.getYear()));

        return card;
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

    public void refreshData() {
        var response = instructorAPI.getSections(Integer.parseInt(instructorId));
        if (response.isSuccess()) {
            List<SectionRow> sections = response.getData();

            cardsContainer.removeAll();

            for (SectionRow section : sections) {
                var courseRes = instructorAPI.getCourse(section.courseCode());
                CourseRow course = courseRes.isSuccess() ? courseRes.getData() : null;
                cardsContainer.add(createSectionCard(section, course), "growx");
            }

            statusLabel.setText(sections.isEmpty() ? "No sections assigned"
                    : sections.size() + " section" + (sections.size() != 1 ? "s" : "") + " assigned");

            cardsContainer.revalidate();
            cardsContainer.repaint();
            scrollPane.revalidate();

        } else {
            statusLabel.setText("Failed to load sections: " + response.getMessage());
            statusLabel.setForeground(UIStyle.ERROR_RED);
        }
    }
}
