package com.example.internetbanking.dto;
import java.math.*;
import jakarta.validation.constraints.*;

public class PayBillRequest {

    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "\\d{10,20}", message = "Account number must be between 10 to 20 digits")
    private String accountNumber;

    @NotBlank(message = "Bill type is required")
    private String billType;

    @Positive(message = "Bill amount must be greater than zero")
    private BigDecimal billAmount;

    @NotBlank(message = "Service number is required")
    @Size(min = 5, max = 20, message = "Service number must be between 5 and 20 characters")
    private String serviceNumber;

 

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

    public BigDecimal getBillAmount() {
        return billAmount;
    }

    public void setBillAmount(BigDecimal billAmount) {
        this.billAmount = billAmount;
    }

    public String getServiceNumber() {
        return serviceNumber;
    }

    public void setServiceNumber(String serviceNumber) {
        this.serviceNumber = serviceNumber;
    }
}
