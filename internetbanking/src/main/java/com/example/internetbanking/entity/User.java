package com.example.internetbanking.entity;
import java.util.UUID;
import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.*;

@Entity
public class User {
    @Id
    private String userId; 
    private String username;
    private String password;
    private String role="CUSTOMER";
    private boolean isVerified = false;
    private String verificationCode;
    private Date codeExpiry;
    private String email;
  
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile profile;

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public boolean isVerified() {
		return isVerified;
	}
	public void setVerified(boolean isVerified) {
		this.isVerified = isVerified;
	}
	public String getVerificationCode() {
		return verificationCode;
	}
	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}
	public Date getCodeExpiry() {
		return codeExpiry;
	}
	public void setCodeExpiry(Date codeExpiry) {
		this.codeExpiry = codeExpiry;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}

}

