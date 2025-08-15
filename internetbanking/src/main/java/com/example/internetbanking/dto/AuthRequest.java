package com.example.internetbanking.dto;
import jakarta.validation.constraints.*;

public class AuthRequest {
	
	@NotBlank(message = "User ID is required")
    private String userId;
	
	 @NotBlank(message = "Password is required")
    private String password;
	 
	public AuthRequest(String userId, String password) {
		super();
		this.userId = userId;
		this.password = password;
	}
	public AuthRequest() {
		super();
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
}
