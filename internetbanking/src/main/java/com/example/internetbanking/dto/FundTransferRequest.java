package com.example.internetbanking.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

public class FundTransferRequest {
	
	@NotBlank(message = "From account number is required")
    @Pattern(regexp = "\\d{10}", message = "From account number must be  10 digits")
    private String fromAccount;
	
	@NotBlank(message = "To account number is required")
    @Pattern(regexp = "\\d{10}", message = "To account number must be  10  digits")
    private String toAccount;
	
	@NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private double amount;
	
    private String transactionType;

    private String transferType;
    private LocalDateTime scheduledDate; 
    private int tpin;

    public int getTpin() {
		return tpin;
	}
	public void setTpin(int tpin) {
		this.tpin = tpin;
	}
	public String getFromAccount() {
        return fromAccount;
    }
    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }
    public String getToAccount() {
        return toAccount;
    }
    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public String getTransferType() {
        return transferType;
    }
    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }
    public void setScheduledDate(LocalDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
    }
}

