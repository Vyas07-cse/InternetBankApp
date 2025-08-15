package com.example.internetbanking.dto;
import jakarta.validation.constraints.*;

public class PasswordRequest {
	
	 @NotBlank(message = "Email is required")
	 @Email(message = "Invalid email format")
	String email;
	 
	 @NotBlank(message = "OTP is required")
	String otp;
	 
	 @NotBlank(message = "Password is required")
	 @Size(min = 6, message = "Password must be at least 6 characters long")
	String password;
	 
	public PasswordRequest() {
		super();
	}
	public String getOtp() {
		return otp;
	}
	public void setOtp(String otp) {
		this.otp = otp;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

}
