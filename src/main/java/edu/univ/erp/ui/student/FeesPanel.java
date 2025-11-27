package edu.univ.erp.ui.student;

import edu.univ.erp.model.Fee;
import edu.univ.erp.service.ServiceException;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.ui.util.UIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FeesPanel extends JPanel {
    private final StudentService studentService;
    private final String studentId;
    private JTable feesTable;
    private DefaultTableModel tableModel;
    private JLabel totalPendingLabel;

    public FeesPanel(StudentService studentService, String studentId) {
        this.studentService = studentService;
        this.studentId = studentId;

        setLayout(new MigLayout("fill, insets 30", "[grow]", "[]20[grow]"));
        setBackground(UIStyle.BACKGROUND_DARK);

        // Header
        JPanel headerPanel = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        headerPanel.setOpaque(false);

        JLabel titleLabel = UIStyle.createHeading("Fee Management", 1);
        headerPanel.add(titleLabel, "growx");

        JButton refreshButton = UIStyle.createSecondaryButton("🔄 Refresh");
        refreshButton.addActionListener(e -> loadFees());
        headerPanel.add(refreshButton);

        add(headerPanel, "growx, wrap");

        // Summary Card
        JPanel summaryCard = UIStyle.createCard();
        summaryCard.setLayout(new MigLayout("insets 20", "[]10[]", "[]"));
        JLabel pendingTitle = UIStyle.createBodyLabel("Total Pending:");
        totalPendingLabel = UIStyle.createHeading("$0.00", 2);
        totalPendingLabel.setForeground(UIStyle.ERROR_RED);
        summaryCard.add(pendingTitle);
        summaryCard.add(totalPendingLabel);
        add(summaryCard, "growx, wrap");

        // Table
        String[] columns = { "ID", "Description", "Due Date", "Amount", "Status", "Action" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only Action button is editable
            }
        };

        feesTable = new JTable(tableModel);
        UIStyle.styleTable(feesTable);

        // Custom renderer/editor for Pay button would be ideal, but for simplicity
        // we'll use a selection listener or simple button column if possible.
        // For this MVP, we will add a "Pay Selected" button below instead of per-row
        // buttons to keep it simple and robust.

        JScrollPane scrollPane = new JScrollPane(feesTable);
        UIStyle.styleScrollPane(scrollPane);
        add(scrollPane, "grow, wrap");

        // Actions
        JPanel actionPanel = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        actionPanel.setOpaque(false);

        JButton payButton = UIStyle.createPrimaryButton("💳 Pay Selected Fee");
        payButton.addActionListener(e -> paySelectedFee());
        actionPanel.add(payButton, "skip, w 200!");

        add(actionPanel, "growx");

        loadFees();
    }

    private void loadFees() {
        try {
            tableModel.setRowCount(0);
            List<Fee> fees = studentService.getFees(Integer.parseInt(studentId));
            double totalPending = 0;

            for (Fee fee : fees) {
                tableModel.addRow(new Object[] {
                        fee.getFeeId(),
                        fee.getDescription(),
                        fee.getDueDate(),
                        "$" + fee.getAmount(),
                        fee.getStatus(),
                        fee.getStatus().equals("PENDING") ? "Pay" : "Paid"
                });

                if ("PENDING".equals(fee.getStatus())) {
                    totalPending += fee.getAmount().doubleValue();
                }
            }

            totalPendingLabel.setText(String.format("$%.2f", totalPending));

        } catch (ServiceException e) {
            JOptionPane.showMessageDialog(this, "Error loading fees: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void paySelectedFee() {
        int row = feesTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a fee to pay.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String status = (String) tableModel.getValueAt(row, 4);
        if ("PAID".equals(status)) {
            JOptionPane.showMessageDialog(this, "This fee is already paid.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int feeId = (int) tableModel.getValueAt(row, 0);
        String amount = (String) tableModel.getValueAt(row, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Proceed to pay " + amount + "?\n(This is a mock payment)",
                "Confirm Payment",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                studentService.payFee(feeId);
                JOptionPane.showMessageDialog(this, "Payment Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadFees();
            } catch (ServiceException e) {
                JOptionPane.showMessageDialog(this, "Payment failed: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
