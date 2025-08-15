package com.example.internetbanking.controller;
import com.example.internetbanking.service.impl.JWTService; 

import com.example.internetbanking.entity.*;
import com.example.internetbanking.exception.UserNotFoundException;
import com.example.internetbanking.repository.*;
import com.example.internetbanking.service.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileServiceImpl profileService;

    @MockBean
    private UserProfileRepository profileRepo;

    @MockBean
    private LogHistoryRepository logHistoryRepository;
    
    @MockBean
    private JWTService jwtService;

    @MockBean
    private NotificationServiceImpl notificationService;

    private UserProfile userProfile;

    @BeforeEach
    void setup() {
        userProfile = new UserProfile();
        userProfile.setUserId("user123");
        userProfile.setEmail("test@example.com");
        userProfile.setName("testuser");
    }

    @Test
    void testGetProfile_Success() throws Exception {
        given(profileRepo.findByUser_UserId("user123")).willReturn(Optional.of(userProfile));

        mockMvc.perform(get("/api/profile/user123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("user123"))
            .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testGetProfile_NotFound() throws Exception {
        given(profileRepo.findByUser_UserId("user123")).willReturn(Optional.empty());

        mockMvc.perform(get("/api/profile/user123"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateProfile_Success() throws Exception {
    	 UserProfile mockProfile = new UserProfile();
    	given(profileService.partialUpdate(eq("user123"), anyMap())).willReturn(mockProfile);
        String jsonPayload = "{\"email\":\"newemail@example.com\"}";

        mockMvc.perform(patch("/api/profile/user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
            .andExpect(status().isOk())
            .andExpect(content().string("Profile updated successfully"));
    }

    @Test
    void testGetLogHistory_Success() throws Exception {
        LogHistory log1 = new LogHistory();
        log1.setLoginTime(LocalDateTime.now().minusDays(1));
        LogHistory log2 = new LogHistory();
        log2.setLoginTime(LocalDateTime.now());

        List<LogHistory> logs = Arrays.asList(log2, log1);
        given(logHistoryRepository.findByUser_UserIdOrderByLoginTimeDesc("user123")).willReturn(logs);

        mockMvc.perform(get("/api/profile/user123/log-history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].loginTime").exists())
            .andExpect(jsonPath("$[1].loginTime").exists());
    }

    @Test
    void testGetLogHistory_Empty_NotFound() throws Exception {
        given(logHistoryRepository.findByUser_UserIdOrderByLoginTimeDesc("user123")).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/profile/user123/log-history"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testSendPasswordOtp_Success() throws Exception {
        given(profileRepo.findByUser_UserId("user123")).willReturn(Optional.of(userProfile));
        willDoNothing().given(profileService).initiatePasswordChange("user123", "test@example.com");

        mockMvc.perform(post("/api/profile/user123/password/otp"))
            .andExpect(status().isOk())
            .andExpect(content().string("Password OTP sent"));
    }

    @Test
    void testChangePassword_Success() throws Exception {
        given(profileRepo.findByUser_UserId("user123")).willReturn(Optional.of(userProfile));
        when(profileService.confirmPasswordChange(
        	    eq("user123"),
        	    eq("test@example.com"),
        	    eq("123456"),
        	    eq("oldpass"),
        	    eq("newpass")
        	)).thenReturn(true);

        String jsonPayload = "{\"otp\":\"123456\", \"oldPassword\":\"oldpass\", \"newPassword\":\"newpass\"}";

        mockMvc.perform(post("/api/profile/user123/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
            .andExpect(status().isOk())
            .andExpect(content().string("Password changed successfully"));
    }

    @Test
    void testInitiateKyc_Success() throws Exception {
        given(profileRepo.findByUser_UserId("user123")).willReturn(Optional.of(userProfile));
        willDoNothing().given(profileService).initiateKyc(eq("user123"), eq("PAN"), eq("ABCDE1234F"), eq("test@example.com"));

        String jsonPayload = "{\"documentType\":\"PAN\", \"documentId\":\"ABCDE1234F\"}";

        mockMvc.perform(post("/api/profile/user123/kyc/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
            .andExpect(status().isAccepted())
            .andExpect(content().string("KYC OTP sent"));
    }

    @Test
    void testVerifyKyc_Success() throws Exception {
        given(profileRepo.findByUser_UserId("user123")).willReturn(Optional.of(userProfile));
        willDoNothing().given(profileService).verifyKycOtp(eq("user123"), eq("123456"), eq("test@example.com"));

        String jsonPayload = "{\"otp\":\"123456\"}";

        mockMvc.perform(post("/api/profile/user123/kyc/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
            .andExpect(status().isOk())
            .andExpect(content().string("KYC verification in progress. Will auto-complete in 5 minutes."));
    }

    @Test
    void testGetNotifications_Success() throws Exception {
        Notification notif1 = new Notification();
        notif1.setNotificationId(1L);
        notif1.setMessage("Test notification 1");
        Notification notif2 = new Notification();
        notif2.setNotificationId(2L);
        notif2.setMessage("Test notification 2");

        given(notificationService.getUserNotifications("user123")).willReturn(Arrays.asList(notif1, notif2));

        mockMvc.perform(get("/api/profile/user123/notifications"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].notificationId").value(1L))
            .andExpect(jsonPath("$[1].message").value("Test notification 2"));
    }

    @Test
    void testMarkNotificationAsRead_Success() throws Exception {
        willDoNothing().given(notificationService).markAsRead("user123", 1L);

        mockMvc.perform(post("/api/profile/user123/notifications/1/read"))
            .andExpect(status().isOk())
            .andExpect(content().string("Notification marked as read"));
    }
    
    
    @Test
    void testUpdateAlerts_Success() throws Exception {
        UserProfile updatedProfile = new UserProfile();
        updatedProfile.setUserId("user123");
        updatedProfile.setLoginAlert(true);
        updatedProfile.setTransactionAlert(false);

        given(profileService.updateAlerts("user123", true, false)).willReturn(updatedProfile);

        String jsonPayload = "{\"loginAlert\":true,\"transactionAlert\":false}";

        mockMvc.perform(put("/api/profile/user123/alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("user123"))
            .andExpect(jsonPath("$.loginAlert").value(true))
            .andExpect(jsonPath("$.transactionAlert").value(false));
    }
   




}
