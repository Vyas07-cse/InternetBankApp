package com.example.internetbanking.dto;
import jakarta.validation.constraints.*;
public class OtpRequest {
	
	@NotBlank(message = "User ID is required")
    private String userId;
	
	@NotBlank(message = "OTP is required")
    @Pattern(regexp = "\\d{4,6}", message = "OTP must be 4 to 6 digits")
    private String otp;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}
