package com.sis.app.ui.student;

import com.sis.app.model.view.CourseSectionView;
import com.sis.app.service.ServiceException;
import com.sis.app.service.StudentService;
import com.sis.app.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

class CourseCatalogPanel extends AbstractStudentPanel {
    private final JPanel cardsContainer;
    private final JLabel statusLabel;
    private final JLabel headerLabel;
    private final List<CourseSectionView> currentCatalog = new ArrayList<>();
    private JScrollPane scrollPane;

    CourseCatalogPanel(StudentService studentService, String studentId) {
        super(studentService, studentId);
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND_DARK);
        
        // Header section with title
        JPanel headerPanel = new JPanel(new MigLayout("fillx, insets 20 24 16 24", "[grow][]", "[]"));
        headerPanel.setOpaque(false);
        headerLabel = UIStyle.createHeading("Course Catalog", 2);
        headerLabel.setIcon(new javax.swing.ImageIcon()); // Space for icon
        headerPanel.add(headerLabel, "growx");
        
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIStyle.FONT_SMALL);
        statusLabel.setForeground(UIStyle.TEXT_SECONDARY);
        headerPanel.add(statusLabel, "align right");
        add(headerPanel, BorderLayout.NORTH);

        // Cards container with grid layout
        cardsContainer = new JPanel();
        cardsContainer.setLayout(new MigLayout("fillx, wrap, insets 20", 
            "[grow 0][grow 0][grow 0]", "[]20[]"));
        cardsContainer.setOpaque(false);
        cardsContainer.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        scrollPane = new JScrollPane(cardsContainer);
        UIStyle.styleScrollPane(scrollPane);
        scrollPane.getViewport().setBackground(UIStyle.BACKGROUND_DARK);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createCourseCard(CourseSectionView view) {
        JPanel card = UIStyle.createCard();
        card.setLayout(new MigLayout("fillx, wrap, insets 20", "[grow]", "[]8[]4[]4[]4[]4[]12[]"));
        card.setPreferredSize(new Dimension(320, 280));
        card.setMinimumSize(new Dimension(300, 260));
        
        // Course title
        JLabel courseLabel = new JLabel(view.getCourseDisplay());
        courseLabel.setFont(UIStyle.FONT_H3);
        courseLabel.setForeground(UIStyle.ACCENT_BLUE);
        card.add(courseLabel, "growx, wrap");
        
        // Section ID badge
        JLabel sectionBadge = new JLabel("Section #" + view.getSectionId());
        sectionBadge.setFont(UIStyle.FONT_SMALL);
        sectionBadge.setForeground(UIStyle.ACCENT_BLUE_HOVER);
        sectionBadge.setBorder(new EmptyBorder(4, 12, 4, 12));
        sectionBadge.setOpaque(true);
        sectionBadge.setBackground(new Color(UIStyle.DEEP_GRAY.getRed(), UIStyle.DEEP_GRAY.getGreen(), 
            UIStyle.DEEP_GRAY.getBlue(), 100));
        card.add(sectionBadge, "growx 0, wrap");
        
        // Details grid
        addDetailRow(card, "📅 Schedule", view.getDayTime());
        addDetailRow(card, "🏢 Room", view.getRoom());
        addDetailRow(card, "📚 Credits", String.valueOf(view.getCredits()));
        addDetailRow(card, "📆 Semester", view.getSemester() + ", " + view.getYear());
        
        // Capacity info
        JPanel capacityPanel = new JPanel(new MigLayout("fillx, insets 8 0 0 0", "[grow]", "[]"));
        capacityPanel.setOpaque(false);
        int available = view.getSeatsAvailable();
        int capacity = view.getCapacity();
        int taken = capacity - available;
        
        JLabel capacityLabel = new JLabel(String.format("Seats: %d/%d available", available, capacity));
        capacityLabel.setFont(UIStyle.FONT_SMALL);
        capacityLabel.setForeground(available > 0 ? UIStyle.SUCCESS_GREEN : UIStyle.ERROR_RED);
        capacityPanel.add(capacityLabel, "growx");
        
        // Progress bar
        JProgressBar progressBar = new JProgressBar(0, capacity);
        progressBar.setValue(taken);
        progressBar.setStringPainted(true);
        progressBar.setString(String.format("%d%%", (taken * 100) / capacity));
        progressBar.setForeground(available > 0 ? UIStyle.ACCENT_BLUE : UIStyle.ERROR_RED);
        progressBar.setBackground(UIStyle.BORDER_COLOR);
        progressBar.setPreferredSize(new Dimension(0, 8));
        capacityPanel.add(progressBar, "growx, wrap");
        
        card.add(capacityPanel, "growx, wrap");
        
        // Register button
        JButton registerBtn = UIStyle.createPrimaryButton("Register");
        registerBtn.setPreferredSize(new Dimension(0, 42));
        registerBtn.setEnabled(available > 0);
        if (available == 0) {
            registerBtn.setText("Full");
            registerBtn.setBackground(UIStyle.BORDER_COLOR);
            registerBtn.setForeground(UIStyle.TEXT_SECONDARY);
        }
        registerBtn.addActionListener(e -> registerForSection(view.getSectionId()));
        card.add(registerBtn, "growx");
        
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
    
    private void registerForSection(int sectionId) {
        try {
            studentService.registerForSection(studentId, sectionId);
            JOptionPane.showMessageDialog(this, 
                "Successfully registered for section " + sectionId + ".",
                "Registration Complete", 
                JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        } catch (ServiceException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), 
                "Registration Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    void refreshData() {
        try {
            List<CourseSectionView> catalog = studentService.getCourseCatalog();
            currentCatalog.clear();
            currentCatalog.addAll(catalog);
            
            // Clear existing cards
            cardsContainer.removeAll();
            
            // Create cards for each course section
            for (CourseSectionView view : catalog) {
                cardsContainer.add(createCourseCard(view), "growx");
            }
            
            // Update status
            statusLabel.setText(catalog.isEmpty() ? "No courses available" : 
                catalog.size() + " course section" + (catalog.size() != 1 ? "s" : "") + " available");
            
            // Refresh layout
            cardsContainer.revalidate();
            cardsContainer.repaint();
            scrollPane.revalidate();
            
        } catch (SQLException ex) {
            statusLabel.setText("Failed to load catalog: " + ex.getMessage());
            statusLabel.setForeground(UIStyle.ERROR_RED);
        }
    }
}

