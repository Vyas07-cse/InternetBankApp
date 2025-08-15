package com.example.internetbanking.service;

import com.example.internetbanking.service.impl.UserProfileServiceImpl;
import com.example.internetbanking.service.impl.EmailServiceImpl;
import com.example.internetbanking.entity.User;
import com.example.internetbanking.entity.UserProfile;
import com.example.internetbanking.exception.InvalidOtpException;
import com.example.internetbanking.exception.InvalidPasswordException;
import com.example.internetbanking.exception.UserNotFoundException;
import com.example.internetbanking.repository.UserProfileRepository;
import com.example.internetbanking.repository.UserRepository;
import com.example.internetbanking.util.OtpUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserProfileServiceImplTest {

    @InjectMocks
    private UserProfileServiceImpl service;

    @Mock
    private UserProfileRepository profileRepo;

    @Mock
    private UserRepository userRepo;


    @Mock
    private OtpUtil otpUtil;

    @Mock
    private EmailServiceImpl emailService;

    private UserProfile userProfile;
    private User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setUsername("testUser");
        user.setEmail("oldemail@example.com");
        user.setPassword("OldPass1!");

        userProfile = new UserProfile();
        userProfile.setUser(user);
        userProfile.setPassword("OldPass1!");
        userProfile.setEmail("oldemail@example.com");
        userProfile.setKycStatus("NONE");
    }

    @Test
    void testPartialUpdate_Success() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("email", "newemail@example.com");
        updates.put("username", "newUser");
        updates.put("address", "New Address");

        when(profileRepo.findByUser_UserId(anyString())).thenReturn(Optional.of(userProfile));
        when(profileRepo.save(any(UserProfile.class))).thenReturn(userProfile);
        when(userRepo.save(any(User.class))).thenReturn(user);

        UserProfile updated = service.partialUpdate("userId", updates);

        assertEquals("newemail@example.com", updated.getEmail());
        assertEquals("newUser", updated.getUser().getUsername());
        assertEquals("New Address", updated.getAddress());

        verify(profileRepo).save(userProfile);
        verify(userRepo).save(user);
    }

   

    @Test
    void testInitiateKyc_InvalidDocType() {
        when(profileRepo.findByUser_UserId(anyString())).thenReturn(Optional.of(userProfile));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.initiateKyc("userId", "INVALID", "123", "email@example.com"));
        assertEquals("Invalid KYC document type. Must be either AADHAR or PAN", ex.getMessage());
    }

    
   
    @Test
    void testVerifyKycOtp_ProfileNotFound() {
        when(profileRepo.findByUser_UserId(anyString())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.verifyKycOtp("userId", "otp", "email@example.com"));
    }
    
   
    
    @Test
    void testUpdateSecurityQuestion_ProfileNotFound() {
        when(profileRepo.findByUser_UserId("missing")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                service.updateSecurityQuestion("missing", "Q", "A")
        );

        verify(profileRepo, never()).save(any());
    }
    
    
    @Test
    void testUpdateAlerts_Success() {
        userProfile.setLoginAlert(false);
        userProfile.setTransactionAlert(false);

        when(profileRepo.findByUser_UserId("userId")).thenReturn(Optional.of(userProfile));
        when(profileRepo.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfile result = service.updateAlerts("userId", true, true);

        assertTrue(result.isLoginAlert());
        assertTrue(result.isTransactionAlert());
        verify(profileRepo).save(userProfile);
    }
    
    
    @Test
    void testUpdateAlerts_ProfileNotFound() {
        when(profileRepo.findByUser_UserId("missing")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                service.updateAlerts("missing", true, false)
        );

        verify(profileRepo, never()).save(any());
    }
    
}
