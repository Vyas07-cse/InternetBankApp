package com.example.internetbanking.controller;

import com.example.internetbanking.dto.*;
import com.example.internetbanking.entity.User;
import com.example.internetbanking.exception.UserNotFoundException;
import com.example.internetbanking.service.impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.example.internetbanking.service.impl.JWTService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServiceImpl userService;

    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private JWTService jwtService; 

    private RegistrationRequest registrationRequest;
    private AuthRequest authRequest;
    private OtpRequest otpRequest;
    private PasswordRequest passwordRequest;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId("U0001");
        user.setEmail("test@example.com");
        user.setUsername("testuser");

        RegistrationRequest request= new RegistrationRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("Test@1234"); 
        request.setPhone("9876543210");    
        request.setAddress("123 Test Street");
        request.setSecurityQuestion("Your favorite color?");
        request.setSecurityAnswer("Blue");

        authRequest = new AuthRequest();
        authRequest.setUserId("U0001");
        authRequest.setPassword("password123");

        otpRequest = new OtpRequest();
        otpRequest.setUserId("U0001");
        otpRequest.setOtp("123456");

        passwordRequest = new PasswordRequest();
        passwordRequest.setEmail("test@example.com");
        passwordRequest.setOtp("123456");
        passwordRequest.setPassword("newpass");
    }

   
    @Test
    void testLogin() throws Exception {
        when(userService.verify(any(AuthRequest.class))).thenReturn("Login successful");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string("Login successful"));
    }

    @Test
    void testVerifyOtpSuccess() throws Exception {
        when(userService.verifyOtp(any(RegisterOtp.class))).thenReturn(true);

        mockMvc.perform(post("/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otpRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string("Email verified successfully"));
    }

    @Test
    void testVerifyOtpFailure() throws Exception {
        when(userService.verifyOtp(any(RegisterOtp.class))).thenReturn(false);

        mockMvc.perform(post("/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otpRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Invalid or expired OTP"));
    }

    @Test
    void testLoginOtp() throws Exception {
        when(userService.loginOtp(any(OtpRequest.class))).thenReturn("jwt-token-123");

        mockMvc.perform(post("/auth/login-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otpRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string("jwt-token-123"));
    }

    @Test
    void testForgotPasswordSuccess() throws Exception {
        when(userService.forgotPassword(anyString())).thenReturn("Reset link sent");

        mockMvc.perform(post("/auth/forgot-password")
                .param("email", "test@example.com"))
            .andExpect(status().isOk())
            .andExpect(content().string("Reset link sent"));
    }

    @Test
    void testForgotPasswordUserNotFound() throws Exception {
        when(userService.forgotPassword(anyString())).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/auth/forgot-password")
                .param("email", "unknown@example.com"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("User not found"));
    }

    @Test
    void testChangePassword() throws Exception {
        when(userService.changePassword(any(PasswordRequest.class))).thenReturn("Password changed successfully");

        mockMvc.perform(post("/auth/Password-change")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string("Password changed successfully"));
    }
}
