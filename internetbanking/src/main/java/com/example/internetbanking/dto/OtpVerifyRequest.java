package com.example.internetbanking.dto;
import jakarta.validation.constraints.*;

public class OtpVerifyRequest {
	
	@NotBlank(message = "Beneficiary ID is required")
	 private String beneficiaryId;
	
	@NotBlank(message = "OTP is required")
	    private String otp;
	    
		public String getBeneficiaryId() {
			return beneficiaryId;
		}
		public void setBeneficiaryId(String beneficiaryId) {
			this.beneficiaryId = beneficiaryId;
		}
		public String getOtp() {
			return otp;
		}
		public void setOtp(String otp) {
			this.otp = otp;
		}
}

