package com.example.internetbanking.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    
    @Column(unique = true)
    private String accountNumber;
    
    @NotBlank(message = "Account type is required")
    private String accountType;
    
    @NotNull(message = "Balance is required")
    @Min(value = 0, message = "Balance cannot be negative")
    private double currentBalance;


    
    @NotNull(message = "tpin is required")
    private int tpin;

    private LocalDateTime createdAt;
    
   

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public double  getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }
    
    public int getTpin() {
		return tpin;
	}

	public void setTpin(int tpin) {
		this.tpin = tpin;
	}

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
