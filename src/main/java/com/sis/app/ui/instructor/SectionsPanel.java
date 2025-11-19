package com.sis.app.ui.instructor;

import com.sis.app.model.Course;
import com.sis.app.model.Section;
import com.sis.app.service.InstructorService;
import com.sis.app.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

class SectionsPanel extends JPanel {
    private final InstructorService instructorService;
    private final String instructorId;
    private final JPanel cardsContainer;
    private final JLabel statusLabel;
    private final JScrollPane scrollPane;

    SectionsPanel(InstructorService instructorService, String instructorId) {
        this.instructorService = instructorService;
        this.instructorId = instructorId;
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND_DARK);
        
        // Header
        JPanel headerPanel = new JPanel(new MigLayout("fillx, insets 20 24 16 24", "[grow][]", "[]"));
        headerPanel.setOpaque(false);
        JLabel headerLabel = UIStyle.createHeading("My Assigned Sections", 2);
        headerPanel.add(headerLabel, "growx");
        
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIStyle.FONT_SMALL);
        statusLabel.setForeground(UIStyle.TEXT_SECONDARY);
        headerPanel.add(statusLabel, "align right");
        add(headerPanel, BorderLayout.NORTH);

        // Cards container
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
    
    private JPanel createSectionCard(Section section, Course course) {
        JPanel card = UIStyle.createCard();
        card.setLayout(new MigLayout("fillx, wrap, insets 20", "[grow]", "[]8[]4[]4[]4[]4[]4[]4[]"));
        card.setPreferredSize(new Dimension(420, 280));
        card.setMinimumSize(new Dimension(380, 260));
        
        // Section ID badge
        JLabel sectionBadge = new JLabel("Section #" + section.getSectionId());
        sectionBadge.setFont(UIStyle.FONT_BODY_BOLD);
        sectionBadge.setForeground(UIStyle.ACCENT_BLUE);
        card.add(sectionBadge, "growx, wrap");
        
        // Course title
        String courseDisplay = course != null ? course.getCode() + " - " + course.getTitle() : section.getCourseId();
        JLabel courseLabel = new JLabel(courseDisplay);
        courseLabel.setFont(UIStyle.FONT_H3);
        courseLabel.setForeground(UIStyle.TEXT_PRIMARY);
        card.add(courseLabel, "growx, wrap");
        
        // Details
        addDetailRow(card, "📚 Credits", course != null ? String.valueOf(course.getCredits()) : "-");
        addDetailRow(card, "📅 Schedule", section.getDayTime());
        addDetailRow(card, "🏢 Room", section.getRoom());
        addDetailRow(card, "👥 Capacity", String.valueOf(section.getCapacity()));
        addDetailRow(card, "📆 Semester", String.valueOf(section.getSemester()));
        addDetailRow(card, "📅 Year", String.valueOf(section.getYear()));
        
        return card;
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

    void refreshData() {
        try {
            List<Section> sections = instructorService.getSectionsForInstructor(instructorId);
            
            cardsContainer.removeAll();
            
            for (Section section : sections) {
                Course course = instructorService.getCourseForSection(section.getSectionId());
                cardsContainer.add(createSectionCard(section, course), "growx");
            }
            
            statusLabel.setText(sections.isEmpty() ? "No sections assigned" : 
                sections.size() + " section" + (sections.size() != 1 ? "s" : "") + " assigned");
            
            cardsContainer.revalidate();
            cardsContainer.repaint();
            scrollPane.revalidate();
            
        } catch (SQLException ex) {
            statusLabel.setText("Failed to load sections: " + ex.getMessage());
            statusLabel.setForeground(UIStyle.ERROR_RED);
        }
    }
}

