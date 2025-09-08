package com.example.internetbanking.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.example.internetbanking.exception.UserNotFoundException;
import com.example.internetbanking.dto.AuthRequest;
import com.example.internetbanking.dto.OtpRequest;
import com.example.internetbanking.dto.PasswordRequest;
import com.example.internetbanking.dto.RegisterOtp;
import com.example.internetbanking.service.impl.UserServiceImpl;
import com.example.internetbanking.dto.RegistrationRequest;
@RestController
@RequestMapping("/auth")
public class LoginController {

    @Autowired
    private UserServiceImpl userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegistrationRequest request) {
        String savedUser = userService.register(request);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody AuthRequest authRequest) {
        String response = userService.verify(authRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody RegisterOtp registerOtp) {
        boolean verified = userService.verifyOtp(registerOtp);
        if (verified) {
            return ResponseEntity.ok("Email verified successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }
    }
    @PostMapping("/login-otp")
    public ResponseEntity<String> loginOtp(@Valid @RequestBody OtpRequest otpRequest){
    	String token=userService.loginOtp(otpRequest);
    	return ResponseEntity.ok(token);
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String response = userService.forgotPassword(request.get("email"));
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }   
    @PostMapping("/Password-change")
    public ResponseEntity<String> changePassword(@Valid @RequestBody PasswordRequest passwordRequest){
    	String response=userService.changePassword(passwordRequest);
    	return ResponseEntity.ok(response);
    }
}
