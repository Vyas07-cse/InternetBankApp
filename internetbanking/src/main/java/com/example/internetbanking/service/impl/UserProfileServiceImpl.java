package com.example.internetbanking.service.impl;

import com.example.internetbanking.entity.User;
import com.example.internetbanking.entity.UserProfile;
import com.example.internetbanking.exception.InvalidOtpException;
import com.example.internetbanking.exception.InvalidPasswordException;
import com.example.internetbanking.exception.UserNotFoundException;
import com.example.internetbanking.repository.UserProfileRepository;
import com.example.internetbanking.repository.UserRepository;
import com.example.internetbanking.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl {

	@Autowired
    private UserProfileRepository profileRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private OtpUtil otpUtil;

    @Autowired
    private EmailServiceImpl emailService;
    private static final Logger logger = Logger.getLogger(UserProfileServiceImpl.class.getName());

   
    private final Map<String, Timer> kycTimers = new HashMap<>();

    public Optional<UserProfile> getProfile(String userId) {
        return profileRepo.findByUser_UserId(userId);
    }

    public UserProfile partialUpdate(String userId, Map<String, Object> updates) {
        logger.info("Updating profile for user " + userId);
        UserProfile profile = profileRepo.findByUser_UserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Profile not found"));
        
        User user = profile.getUser();
        if (user == null) {
            throw new UserNotFoundException("Linked User not found");
        }
        updates.forEach((key, value) -> {
            switch (key) {
                case "address" -> profile.setAddress((String) value);
                case "email" -> {
                	  profile.setEmail((String) value);
                	  user.setEmail((String) value);
                }
                case "phone" ->
                {profile.setPhone((String) value); 
                }
                case "name" -> 
                {
                	user.setUsername((String) value);
                    profile.setName((String)value);
                }
                }
          
        });
        userRepo.save(user);
        return profileRepo.save(profile);
    }

   
    public void initiatePasswordChange(String userId, String email) {
        String otp =generateOtp();
        User user=userRepo.findByUserId(userId);
        user.setVerificationCode(otp);
        emailService.sendOtpEmail(email, otp);
        userRepo.save(user);
        logger.info("Password change OTP sent to " + email);
    }
    private String generateOtp() {
        int otp = (int)(Math.random() * 9000) + 1000; 
        return String.valueOf(otp);
    }
   

    public boolean confirmPasswordChange(String userId, String email, String otp, String oldPwd, String newPwd) {
    	User user=userRepo.findByUserId(userId);
    
        if (!user.getVerificationCode().matches(otp)) {
            throw new InvalidOtpException("Invalid or expired OTP");
        }

        UserProfile profile = profileRepo.findByUser_UserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Profile not found"));

       BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

if (!passwordEncoder.matches(oldPwd, profile.getPassword())) {
    throw new InvalidPasswordException("Old password is incorrect");
}


        if (oldPwd.equals(newPwd)) {
            throw new IllegalArgumentException("New password cannot be the same as old password");
        }

        
        String encodedPwd = passwordEncoder.encode(newPwd);
        profile.setPassword(encodedPwd);
        profileRepo.save(profile);

        if (user != null) {
            user.setPassword(encodedPwd);
            userRepo.save(user);
        }

        logger.info("Password changed for user: " + userId);
        return true;
    }

    public void initiateKyc(String userId, String docType, String docId, String email) {
        UserProfile profile = profileRepo.findByUser_UserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Profile not found"));

        if (docType == null || docId == null) {
            throw new IllegalArgumentException("Document type and document ID must be provided");
        }

        String docTypeUpper = docType.toUpperCase(Locale.ROOT);
        boolean valid;

        switch (docTypeUpper) {
            case "AADHAR" -> {
                valid = docId.matches("^[2-9]{1}\\d{11}$");
                if (!valid) throw new IllegalArgumentException("Invalid Aadhar number format");
            }
            case "PAN" -> {
                valid = docId.matches("^[A-Z]{5}\\d{4}[A-Z]{1}$");
                if (!valid) throw new IllegalArgumentException("Invalid PAN card number format");
            }
            default -> throw new IllegalArgumentException("Invalid KYC document type. Must be either AADHAR or PAN");
        }

        String otp = generateOtp();
       User user=userRepo.findByUserId(userId);
        user.setVerificationCode(otp);
        userRepo.save(user);
        emailService.sendOtpEmail(email, otp);

        profile.setKycStatus("OTP_PENDING");
        profile.setKycDocumentType(docTypeUpper);
        profile.setKycDocumentId(docId);
        profileRepo.save(profile);

        logger.info("KYC initiation OTP sent for user " + userId + ", document: " + docTypeUpper);

        if (kycTimers.containsKey(userId)) {
            kycTimers.get(userId).cancel();
        }
    }

    public void verifyKycOtp(String userId, String otp, String email) {
        UserProfile profile = profileRepo.findByUser_UserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Profile not found"));
        Optional<User> uli=userRepo.findById(userId);
        User user=uli.get();
        if (!user.getVerificationCode().matches(otp)) {
            throw new InvalidOtpException("Invalid or expired KYC OTP");
        }

        profile.setKycStatus("IN_PROGRESS");
        profileRepo.save(profile);

        Timer timer = new Timer();
        kycTimers.put(userId, timer);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                profile.setKycStatus("VERIFIED");
                profileRepo.save(profile);
                logger.info("KYC auto-verified for user: " + userId);
                kycTimers.remove(userId);
            }
        }, 300_000L);
    }
    
    public void updateSecurityQuestion(String userId, String question, String answer) {
    UserProfile profile = profileRepo.findByUser_UserId(userId)
            .orElseThrow(() -> new UserNotFoundException("Profile not found"));
    profile.setSecurityQuestion(question);
    profile.setSecurityAnswer(answer);
    profileRepo.save(profile);
}

    
    
    public UserProfile updateAlerts(String userId, Boolean loginAlert, Boolean transactionAlert) {
        UserProfile profile = profileRepo.findByUser_UserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Profile not found"));

        if (loginAlert != null) {
            profile.setLoginAlert(loginAlert);
        }
        if (transactionAlert != null) {
            profile.setTransactionAlert(transactionAlert);
        }

        return profileRepo.save(profile);
    }



    
}