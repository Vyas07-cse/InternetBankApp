package com.example.internetbanking.controller;

import com.example.internetbanking.entity.UserProfile;
import com.example.internetbanking.entity.Notification;
import com.example.internetbanking.service.impl.NotificationServiceImpl;
import com.example.internetbanking.exception.UserNotFoundException;
import com.example.internetbanking.repository.UserProfileRepository;
import com.example.internetbanking.service.impl.UserProfileServiceImpl;
import org.springframework.http.*;
import com.example.internetbanking.entity.LogHistory;
import com.example.internetbanking.repository.LogHistoryRepository;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

	@Autowired
    private UserProfileServiceImpl profileService;

    @Autowired
    private UserProfileRepository profileRepo;
    
    @Autowired
    private LogHistoryRepository logHistoryRepository;

    @Autowired
    private NotificationServiceImpl notificationService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfile> getProfile(@PathVariable String userId) {
        return profileRepo.findByUser_UserId(userId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));
    }

    @PatchMapping("/update/{userId}")
    public ResponseEntity<String> updateProfile(@PathVariable String userId, @RequestBody Map<String, Object> updates) {
        profileService.partialUpdate(userId, updates);
        return ResponseEntity.ok("Profile updated successfully");
    }
    
    
    @GetMapping("/{userId}/log-history")
    public ResponseEntity<List<LogHistory>> getLogHistory(@PathVariable String userId) {
        List<LogHistory> history = logHistoryRepository.findByUser_UserIdOrderByLoginTimeDesc(userId);
        if (history.isEmpty()) {
        	throw new UserNotFoundException("No login history found for user with ID " + userId);
        }
        return ResponseEntity.ok(history);
    }

    
    @PostMapping("/{userId}/password/otp")
    public ResponseEntity<String> sendPasswordOtp(@PathVariable String userId) {
        UserProfile profile = profileRepo.findByUser_UserId(userId).orElseThrow();
        profileService.initiatePasswordChange(userId, profile.getEmail());
        return ResponseEntity.ok("Password OTP sent");
    }

    
    @PostMapping("/{userId}/change-password")
    public ResponseEntity<String> changePassword(@PathVariable String userId, @RequestBody Map<String, String> payload) {
        UserProfile profile = profileRepo.findByUser_UserId(userId).orElseThrow();
        profileService.confirmPasswordChange(
                userId,
                profile.getEmail(),
                payload.get("otp"),
                payload.get("oldPassword"),
                payload.get("newPassword")
        );
        return ResponseEntity.ok("Password changed successfully");
    }

 
    @PostMapping("/{userId}/kyc/initiate")
    public ResponseEntity<String> initiateKyc(@PathVariable String userId, @RequestBody Map<String, String> payload) {
        String docType = payload.get("documentType");
        String docId = payload.get("documentId");
        UserProfile profile = profileRepo.findByUser_UserId(userId).orElseThrow();
        profileService.initiateKyc(userId, docType, docId, profile.getEmail());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("KYC OTP sent");
    }

    
    @PostMapping("/{userId}/kyc/verify")
    public ResponseEntity<String> verifyKyc(@PathVariable String userId, @RequestBody Map<String, String> payload) {
        UserProfile profile = profileRepo.findByUser_UserId(userId).orElseThrow();
        profileService.verifyKycOtp(userId, payload.get("otp"), profile.getEmail());
        return ResponseEntity.ok("KYC verification in progress. Will auto-complete in 5 minutes.");
    }

     @GetMapping("/{userId}/notifications")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }
    
    
    @PostMapping("/{userId}/notifications/{notificationId}/read")
    public ResponseEntity<String> markNotificationAsRead(@PathVariable String userId, @PathVariable Long notificationId) {
        notificationService.markAsRead(userId, notificationId);
        return ResponseEntity.ok("Notification marked as read");
    }
    
    @PutMapping("/{userId}/security-question/initiate")
    public ResponseEntity<String> initiateSecurityQuestionUpdate(@PathVariable String userId) {
        profileService.initiateSecurityQuestionOtp(userId);
        return ResponseEntity.ok("Security Question OTP sent");
    }

    @PutMapping("/{userId}/security-question/confirm")
    public ResponseEntity<String> updateSecurityQuestion(
            @PathVariable String userId,
            @RequestBody Map<String, String> payload) {
        profileService.confirmSecurityQuestionChange(
                userId,
                payload.get("otp"),
                payload.get("securityQuestion"),
                payload.get("securityAnswer")
        );
        return ResponseEntity.ok("Security question updated successfully");
    }
    
    @PutMapping("/{userId}/alerts")
    public ResponseEntity<UserProfile> updateAlerts(
            @PathVariable String userId,
            @RequestBody Map<String, Boolean> payload) {
        return ResponseEntity.ok(
                profileService.updateAlerts(
                        userId,
                        payload.get("loginAlert"),
                        payload.get("transactionAlert")
                )
        );
    }


    
}