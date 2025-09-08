package com.example.internetbanking.service.impl;

import java.time.LocalDateTime;
import java.util.*;
import com.example.internetbanking.entity.UserProfile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.internetbanking.repository.LogHistoryRepository;
import com.example.internetbanking.repository.UserProfileRepository;
import com.example.internetbanking.exception.CustomException;
import com.example.internetbanking.exception.UserNotFoundException;
import com.example.internetbanking.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import com.example.internetbanking.dto.AuthRequest;
import com.example.internetbanking.dto.OtpRequest;
import com.example.internetbanking.dto.PasswordRequest;
import com.example.internetbanking.dto.RegisterOtp;
import com.example.internetbanking.entity.LogHistory;
import com.example.internetbanking.entity.User;
import com.example.internetbanking.dto.RegistrationRequest;
@Service
public class UserServiceImpl {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private LogHistoryRepository logHistory;

     @Autowired
     private HttpServletRequest request;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public String register(RegistrationRequest request) {
    	boolean us=userRepo.existsByEmail(request.getEmail());
    	if(us) {
    		throw new CustomException("User already Exists");
    	}
    	User user=new User();
    	String id=generateuserId();
    	user.setUserId(id);
        user.setUsername(request.getName());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole("CUSTOMER");
        user.setVerified(false);

        String otp = generateVerificationCode();
        user.setVerificationCode(otp);
      

        User savedUser = userRepo.save(user);
        userRepo.flush();
        
        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);                       
        profile.setName(request.getName());
        profile.setEmail(savedUser.getEmail());
        profile.setPhone(request.getPhone());
        profile.setAddress(request.getAddress());
        profile.setPassword(savedUser.getPassword());
        profile.setSecurityQuestion(request.getSecurityQuestion());
        profile.setSecurityAnswer(request.getSecurityAnswer());
        profile.setTransactionAlert(true);
        profile.setKycDocumentId(null);
        profile.setKycDocumentType(null);
        profile.setKycStatus("Pending");
        profile.setLoginAlert(true);

        userProfileRepository.save(profile); 
        sendOtp(user.getEmail(), otp);

        return id;
    }

    public String verify(AuthRequest authRequest) {
        try {
            User user = userRepo.findByUserId(authRequest.getUserId());
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            if (!user.isVerified()) {
                throw new RuntimeException("Email not verified");
            }

            Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUserId(), authRequest.getPassword())
            );

            if (authentication.isAuthenticated()) {
            	String otp=generateVerificationCode();
            	user.setVerificationCode(otp);
            	sendOtp(user.getEmail(), otp);
            	userRepo.save(user);
                return "Otp Sent for 2FA";
            } else {
                throw new RuntimeException("Invalid authentication");
            }
        } catch (AuthenticationException e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    public String generateVerificationCode() {
        int code = (int)(Math.random() * 9000) + 1000;
        return String.valueOf(code);
    }
    public void sendInfo(String toEmail,String msg) {
    	SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Registration Successful!");
        message.setText(msg);
        mailSender.send(message);
		
	}

    public void sendOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Email Verification OTP");
        message.setText("Dear User,\n\nYour OTP for email verification is: " + otp +
                        "\n\nThis OTP is valid for 10 minutes.\n\nThank you,\nBank Team");
        mailSender.send(message);
    }

    public boolean verifyOtp(RegisterOtp registerOtp) {
        User user = userRepo.findByEmail(registerOtp.getEmail());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (user.isVerified()) {
            return true; 
        }

        if (user.getVerificationCode() != null && user.getVerificationCode().equals(registerOtp.getOtp())) {
            user.setVerified(true);
            user.setVerificationCode(null);
            String msg="Account created Successfully with id "+user.getUserId();
            sendInfo(user.getEmail(),msg);
            userRepo.save(user);
            
            return true;
        } else {
            return false;
        }
    }

    public String loginOtp(OtpRequest otpRequest) {
		 User user=userRepo.findByUserId(otpRequest.getUserId());
		 if(user.getVerificationCode().equals(otpRequest.getOtp())) {
			 user.setVerified(true);
			 userRepo.save(user);
			 
			 LogHistory log = new LogHistory();
		        log.setUser(user);
		        log.setLoginTime(LocalDateTime.now());
		        log.setIpAddress(getClientIP(request));
		        logHistory.save(log);
            String token = jwtService.generateToken(otpRequest.getUserId());
            return token;
		 }
		 else {
		throw new CustomException("Invalid Otp");
		 }
	}
	
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }



	public String forgotPassword(String email) throws UserNotFoundException {
		User user = userRepo.findByEmail(email.trim().toLowerCase());
	    
	    if (user == null) {
	        throw new UserNotFoundException("No user found with email: " + email);
	    }

	    String otp = generateVerificationCode();
	    user.setVerificationCode(otp);
	    userRepo.save(user);
	    sendOtp(email, otp);

	    return "OTP sent for password change request";
	}

	@Transactional
    @Scheduled(fixedDelay = 60000) 
    public void deleteExpiredNonVerifiedUsers() {
        Date now = new Date();


        List<User> expiredUsers = userRepo.findAllByIsVerifiedFalseAndCodeExpiryBefore(now);

        if (!expiredUsers.isEmpty()) {
            userRepo.deleteAll(expiredUsers);
            System.out.println("Deleted " + expiredUsers.size() + " expired, non-verified users.");
        }
    }

	public String changePassword(PasswordRequest passwordRequest) {
		User user=userRepo.findByEmail(passwordRequest.getEmail());
		 if (user == null) {
		        throw new CustomException("User not found");
		    }
		if(passwordRequest.getOtp().equals(user.getVerificationCode())) {
			user.setPassword(encoder.encode(passwordRequest.getPassword()));
			userRepo.save(user);
			
			Optional<UserProfile> profileOpt = userProfileRepository.findByUser_UserId(user.getUserId());
			if (profileOpt.isPresent()) {
			    UserProfile profile = profileOpt.get();
			    profile.setPassword(user.getPassword());
			    userProfileRepository.save(profile);
			}
			return "Password Changed Successfully";
		}
		throw new CustomException("Invalid otp");
	}

	public String generateuserId() {
		int code = (int)(Math.random() * 9000) + 1000;
		return "USBK"+code;
	}
}

