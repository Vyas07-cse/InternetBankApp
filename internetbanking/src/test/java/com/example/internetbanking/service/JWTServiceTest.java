package com.example.internetbanking.service;

import static org.junit.jupiter.api.Assertions.*;
import com.example.internetbanking.service.impl.JWTService;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Date;

public class JWTServiceTest {

    private JWTService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JWTService();
    }

    @Test
    void testGenerateTokenAndExtractUserId() {
        String userId = "user123";
        String token = jwtService.generateToken(userId);

        assertNotNull(token);

        String extractedUserId = jwtService.extractUserId(token);
        assertEquals(userId, extractedUserId);
    }

    @Test
    void testExtractExpiration_NotExpired() {
        String userId = "user123";
        String token = jwtService.generateToken(userId);

        Date expiration = jwtService.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testValidateToken_Success() {
        String userId = "user123";
        String token = jwtService.generateToken(userId);

        UserDetails userDetails = User.withUsername(userId).password("password").authorities("ROLE_USER").build();

        boolean isValid = jwtService.validateToken(token, userDetails);
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidUser() {
        String token = jwtService.generateToken("user123");
        UserDetails userDetails = User.withUsername("otherUser").password("password").authorities("ROLE_USER").build();

        boolean isValid = jwtService.validateToken(token, userDetails);
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_ExpiredToken() throws Exception {
        JWTService shortExpiryJwtService = new JWTService() {
            @Override
            public String generateToken(String userId) {
                try {
                   
                    java.lang.reflect.Method method = JWTService.class.getDeclaredMethod("getKey");
                    method.setAccessible(true);
                    javax.crypto.SecretKey key = (javax.crypto.SecretKey) method.invoke(this);

                    long nowMillis = System.currentTimeMillis();
                    long expMillis = nowMillis + 1000;
                    Date now = new Date(nowMillis);
                    Date exp = new Date(expMillis);
                    return io.jsonwebtoken.Jwts.builder()
                            .setSubject(userId)
                            .setIssuedAt(now)
                            .setExpiration(exp)
                            .signWith(key, io.jsonwebtoken.SignatureAlgorithm.HS256)
                            .compact();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        String token = shortExpiryJwtService.generateToken("user123");

        
        Thread.sleep(1500);

        UserDetails userDetails = User.withUsername("user123").password("password").authorities("ROLE_USER").build();

        boolean isValid;
        try {
            isValid = shortExpiryJwtService.validateToken(token, userDetails);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            isValid = false;
        }
        assertFalse(isValid);
    }
}
