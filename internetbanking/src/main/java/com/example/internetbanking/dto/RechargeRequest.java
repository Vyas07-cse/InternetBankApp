package com.example.internetbanking.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class RechargeRequest {
	
	@NotBlank(message = "Account number is required")
    @Pattern(regexp = "\\d{10,20}", message = "Account number must be between 10 and 20 digits")
    private String accountNumber;
	
	@Min(value = 1000, message = "TPIN must be a 4-digit number")
    @Max(value = 9999, message = "TPIN must be a 4-digit number")
    private int tpin;
	
	@NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least ₹1.00")
    private BigDecimal amount;

    
    public String getAccountNumber() {
        return accountNumber;
    }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public int getTpin() {
        return tpin;
    }
    public void setTpin(int tpin) {
        this.tpin = tpin;
    }

    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
