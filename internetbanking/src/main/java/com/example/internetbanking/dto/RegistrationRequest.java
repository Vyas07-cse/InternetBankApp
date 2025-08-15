package com.example.internetbanking.dto;
import jakarta.validation.constraints.*;

public class RegistrationRequest {
	
	@NotBlank(message = "Name is required")
    private String name;
	
	@NotBlank(message = "Email is required")
    private String email;
	
	@NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
		    message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    private String password;
	
	@NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone number must be a valid 10-digit Indian number")
    private String phone;
	
	@NotBlank(message = "Address is required")
    private String address;
	
	@NotBlank(message = "Security question is required")
    private String securityQuestion;
	
	@NotBlank(message = "Security answer is required")
    private String securityAnswer;

    public RegistrationRequest() {
    }

    public RegistrationRequest(String name, String email, String password,
                               String phone, String address, String securityQuestion, String securityAnswer) {
        
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }

    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }
}

