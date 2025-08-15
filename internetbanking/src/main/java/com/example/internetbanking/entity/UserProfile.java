package com.example.internetbanking.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    private String userId;

    private String username;
    private String email;
    private String phone;
    private String address;
    private String password;
    private boolean transactionAlert = true;
    private boolean loginAlert = true;
    private String securityQuestion;
    private String securityAnswer;
    private String kycStatus;
    private String kycDocumentId;
    private String kycDocumentType;
   
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    
    public UserProfile() {
    }

    
    public UserProfile(String username, String email, String phone, String address, String password,
                       boolean transactionAlert, boolean loginAlert, String securityQuestion, String securityAnswer,
                       String kycStatus, String kycDocumentId, String kycDocumentType, User user) {
        
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.password = password;
        this.transactionAlert = transactionAlert;
        this.loginAlert = loginAlert;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.kycStatus = kycStatus;
        this.kycDocumentId = kycDocumentId;
        this.kycDocumentType = kycDocumentType;
        this.user = user;
    }

   

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return username;
    }

    public void setName(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isTransactionAlert() {
        return transactionAlert;
    }

    public void setTransactionAlert(boolean transactionAlert) {
        this.transactionAlert = transactionAlert;
    }

    public boolean isLoginAlert() {
        return loginAlert;
    }

    public void setLoginAlert(boolean loginAlert) {
        this.loginAlert = loginAlert;
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

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }

    public String getKycDocumentId() {
        return kycDocumentId;
    }

    public void setKycDocumentId(String kycDocumentId) {
        this.kycDocumentId = kycDocumentId;
    }

    public String getKycDocumentType() {
        return kycDocumentType;
    }

    public void setKycDocumentType(String kycDocumentType) {
        this.kycDocumentType = kycDocumentType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    


 
    @Override
    public String toString() {
        return "UserProfile{" +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", password='" + password + '\'' +
                ", transactionAlert=" + transactionAlert +
                ", loginAlert=" + loginAlert +
                ", securityQuestion='" + securityQuestion + '\'' +
                ", securityAnswer='" + securityAnswer + '\'' +
                ", kycStatus='" + kycStatus + '\'' +
                ", kycDocumentId='" + kycDocumentId + '\'' +
                ", kycDocumentType='" + kycDocumentType + '\'' +
                ", user=" + user +
                '}';
    }
}
