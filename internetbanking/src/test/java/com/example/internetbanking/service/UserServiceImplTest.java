package com.example.internetbanking.service;

import com.example.internetbanking.dto.AuthRequest;
import com.example.internetbanking.dto.OtpRequest;
import com.example.internetbanking.dto.PasswordRequest;
import com.example.internetbanking.dto.RegistrationRequest;
import com.example.internetbanking.entity.LogHistory;
import com.example.internetbanking.entity.User;
import com.example.internetbanking.entity.UserProfile;
import com.example.internetbanking.exception.UserNotFoundException;
import com.example.internetbanking.repository.LogHistoryRepository;
import com.example.internetbanking.repository.UserProfileRepository;
import com.example.internetbanking.repository.UserRepository;
import com.example.internetbanking.service.impl.JWTService;
import com.example.internetbanking.service.impl.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.SimpleMailMessage;
import static org.mockito.ArgumentMatchers.any;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepo;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JWTService jwtService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private LogHistoryRepository logHistoryRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private UserProfileRepository userProfileRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister_Success() {
        RegistrationRequest request = new RegistrationRequest();
        request.setName("John Doe");
        request.setPassword("Password@123");
        request.setEmail("john@example.com");
        request.setPhone("1234567890");
        request.setAddress("123 Street");
        request.setSecurityQuestion("Your pet's name?");
        request.setSecurityAnswer("Fluffy");

        User savedUser = new User();
        savedUser.setUserId("user123");
        savedUser.setUsername(request.getName());
        savedUser.setPassword("encodedPassword");
        savedUser.setEmail(request.getEmail());
        savedUser.setRole("CUSTOMER");
        savedUser.setVerified(false);
        savedUser.setVerificationCode("otp");
        savedUser.setCodeExpiry(new Date());

        when(userRepo.save(any(User.class))).thenReturn(savedUser);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(new UserProfile());

        // Do nothing when sending email
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        String result = userService.register(request);

        assertNotNull(result);
        assertEquals("user123", result);
        verify(userRepo).save(any(User.class));
        verify(userProfileRepository).save(any(UserProfile.class));
        verify(mailSender).send(any(SimpleMailMessage.class));

    }

    @Test
    void testVerify_UserNotFound() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUserId("user123");
        authRequest.setPassword("password");

        when(userRepo.findByUserId("user123")).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.verify(authRequest);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testVerify_EmailNotVerified() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUserId("user123");
        authRequest.setPassword("password");

        User user = new User();
        user.setVerified(false);

        when(userRepo.findByUserId("user123")).thenReturn(user);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.verify(authRequest);
        });

        assertEquals("Email not verified", exception.getMessage());
    }

    @Test
    void testVerify_Success() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUserId("user123");
        authRequest.setPassword("password");

        User user = new User();
        user.setVerified(true);
        user.setEmail("john@example.com");

        when(userRepo.findByUserId("user123")).thenReturn(user);

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        when(userRepo.save(any(User.class))).thenReturn(user);

        String response = userService.verify(authRequest);

        assertEquals("Otp Sent for 2FA", response);
        verify(userRepo).save(user);
        verify(mailSender).send(any(SimpleMailMessage.class));

    }

    @Test
    void testVerify_InvalidAuthentication() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUserId("user123");
        authRequest.setPassword("password");

        User user = new User();
        user.setVerified(true);

        when(userRepo.findByUserId("user123")).thenReturn(user);

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.verify(authRequest);
        });

        assertEquals("Invalid authentication", exception.getMessage());
    }

    @Test
    void testVerify_AuthenticationException() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUserId("user123");
        authRequest.setPassword("password");

        User mockUser = new User();
        mockUser.setVerified(true);  

        when(userRepo.findByUserId("user123")).thenReturn(mockUser);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Bad credentials") {});

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.verify(authRequest);
        });

        System.out.println("Exception message: " + exception.getMessage()); 

        assertTrue(exception.getMessage().contains("Authentication failed"));
    }

   

    @Test
    void testLoginOtp_Success() {
        User user = new User();
        user.setUserId("user123");
        user.setVerificationCode("1234");

        when(userRepo.findByUserId("user123")).thenReturn(user);
        when(userRepo.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken("user123")).thenReturn("jwt-token");
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(logHistoryRepository.save(any(LogHistory.class))).thenReturn(new LogHistory());

        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setUserId("user123");
        otpRequest.setOtp("1234");

        String token = userService.loginOtp(otpRequest);

        assertEquals("jwt-token", token);
        verify(userRepo).save(user);
        verify(logHistoryRepository).save(any(LogHistory.class));
    }

    @Test
    void testLoginOtp_InvalidOtp() {
        User user = new User();
        user.setUserId("user123");
        user.setVerificationCode("1234");

        when(userRepo.findByUserId("user123")).thenReturn(user);

        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setUserId("user123");
        otpRequest.setOtp("9999");

        String response = userService.loginOtp(otpRequest);

        assertEquals("Invalid Otp", response);
        verify(userRepo, never()).save(any());
        verify(logHistoryRepository, never()).save(any());
    }

    @Test
    void testForgotPassword_UserNotFound() {
        when(userRepo.findByEmail("test@example.com")).thenReturn(null);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.forgotPassword("test@example.com");
        });

        assertEquals("No user found with email: test@example.com", exception.getMessage());
    }

    @Test
    void testForgotPassword_Success() {
        User user = new User();
        user.setEmail("test@example.com");

        when(userRepo.findByEmail("test@example.com")).thenReturn(user);
        when(userRepo.save(any(User.class))).thenReturn(user);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        String response = userService.forgotPassword("test@example.com");

        assertEquals("OTP sent for password change request", response);
        verify(userRepo).save(user);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testChangePassword_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setVerificationCode("1234");

        when(userRepo.findByEmail("test@example.com")).thenReturn(user);
        when(userRepo.save(any(User.class))).thenReturn(user);

        PasswordRequest passwordRequest = new PasswordRequest();
        passwordRequest.setEmail("test@example.com");
        passwordRequest.setOtp("1234");
        passwordRequest.setPassword("NewPassword123");

        String response = userService.changePassword(passwordRequest);

        assertEquals("Password Changed Successfully", response);
        verify(userRepo).save(user);
    }

    @Test
    void testChangePassword_InvalidOtp() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setVerificationCode("1234");

        when(userRepo.findByEmail("test@example.com")).thenReturn(user);

        PasswordRequest passwordRequest = new PasswordRequest();
        passwordRequest.setEmail("test@example.com");
        passwordRequest.setOtp("9999");
        passwordRequest.setPassword("NewPassword123");

        String response = userService.changePassword(passwordRequest);

        assertEquals("Invalid otp", response);
        verify(userRepo, never()).save(any());
    }
}
