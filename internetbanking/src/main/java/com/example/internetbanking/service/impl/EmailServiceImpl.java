package com.example.internetbanking.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class EmailServiceImpl {

    private static final Logger logger = Logger.getLogger(EmailServiceImpl.class.getName());

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Your OTP for Internet Banking");
        msg.setText("Your OTP is: " + otp + "\nThis OTP is valid for 5 minutes.");
        try {
            mailSender.send(msg);
            logger.info("Sent OTP email to " + to);
        } catch (MailException ex) {
            logger.severe("Failed to send OTP email: " + ex.getMessage());
            throw new RuntimeException("Failed to send OTP email.");
        }
    }
}

