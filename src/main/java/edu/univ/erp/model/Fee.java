package edu.univ.erp.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Fee {
    private int feeId;
    private String studentId;
    private BigDecimal amount;
    private String description;
    private LocalDate dueDate;
    private String status; // PENDING, PAID

    public Fee() {
    }

    public Fee(int feeId, String studentId, BigDecimal amount, String description, LocalDate dueDate, String status) {
        this.feeId = feeId;
        this.studentId = studentId;
        this.amount = amount;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
    }

    public int getFeeId() {
        return feeId;
    }

    public void setFeeId(int feeId) {
        this.feeId = feeId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
