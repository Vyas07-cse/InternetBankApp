package com.example.internetbanking.util;

public class OtpDetails {
    private String otp;
    private long expiresAt;

    public OtpDetails() {
    }

    public OtpDetails(String otp, long expiresAt) {
        this.otp = otp;
        this.expiresAt = expiresAt;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public String toString() {
        return "OtpDetails{" +
                "otp='" + otp + '\'' +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
