package edu.univ.erp;

import edu.univ.erp.model.User;
import edu.univ.erp.ui.MainFrame;
import edu.univ.erp.ui.student.CourseCatalogPanel;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

public class UITest {
    @Test
    public void testCourseCatalogPopulation() throws Exception {
        System.out.println("UI_TEST_START");

        // Setup User
        User student = new User();
        student.setUserId(1001);
        student.setUsername("student1");
        student.setRole("STUDENT");

        try (java.sql.Connection conn = edu.univ.erp.config.DatabaseManager.getInstance().getErpConnection()) {
            System.out.println("UITest Connected to: " + conn.getMetaData().getURL());
            System.out.println("UITest User: " + conn.getMetaData().getUserName());
        }

        // Ensure database has data (mimic SISApplication)
        edu.univ.erp.util.DatabaseSeeder.seedIfEmpty();

        // Initialize MainFrame
        // We need to run this on EDT ideally, but for checking component count it might
        // be okay off-thread
        // or we use SwingUtilities.invokeAndWait

        SwingUtilities.invokeAndWait(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.showForUser(student);

                // Access private fields to check state
                Field catalogPanelField = MainFrame.class.getDeclaredField("studentCatalogPanel");
                catalogPanelField.setAccessible(true);
                CourseCatalogPanel catalogPanel = (CourseCatalogPanel) catalogPanelField.get(frame);

                // Force refresh just in case
                catalogPanel.refreshData();

                Field cardsContainerField = CourseCatalogPanel.class.getDeclaredField("cardsContainer");
                cardsContainerField.setAccessible(true);
                JPanel cardsContainer = (JPanel) cardsContainerField.get(catalogPanel);

                int componentCount = cardsContainer.getComponentCount();
                System.out.println("Cards Container Component Count: " + componentCount);

                if (componentCount > 0) {
                    System.out.println("SUCCESS: UI populated with " + componentCount + " course cards.");
                } else {
                    System.out.println("FAILURE: UI is empty.");
                    org.junit.jupiter.api.Assertions.fail("UI is empty - no course cards found");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        System.out.println("UI_TEST_END");
    }
}
