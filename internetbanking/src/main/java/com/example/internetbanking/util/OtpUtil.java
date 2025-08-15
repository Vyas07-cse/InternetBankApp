package com.example.internetbanking.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpUtil {

    private static final SecureRandom random = new SecureRandom();

    public String generateOtp() {
        return String.format("%06d", random.nextInt(999_999));
    }
}

