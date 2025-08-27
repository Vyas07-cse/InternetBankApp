package com.example.internetbanking.entity;
import jakarta.validation.constraints.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_payments")
public class ScheduledPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;
    
    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "\\d{10,20}", message = "Account number must be between 10 and 20 digits")
    private String accountNumber;
    
    @NotBlank(message = "Bill type is required")
    private String billType;
    
    @NotNull(message = "Amount is required")
    private double amount;
    
    private LocalDateTime scheduledDate;
    private String status; 
    
    @NotBlank(message = "Service number is required")
    @Size(min = 5, max = 20, message = "Service number must be between 5 and 20 characters")
    private String serviceNumber;
    

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBillType() {
        return billType;
    }

    public void setBillType(String billType) {
        this.billType = billType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getServiceNumber() {
        return serviceNumber;
    }

    public void setServiceNumber(String serviceNumber) {
        this.serviceNumber = serviceNumber;
    }
}
