package com.example.internetbanking.controller;
import com.example.internetbanking.service.impl.JWTService;
import com.example.internetbanking.dto.RechargeRequest;
import com.example.internetbanking.entity.Account;
import com.example.internetbanking.entity.Transaction;
import com.example.internetbanking.entity.User;
import com.example.internetbanking.repository.AccountRepository;
import com.example.internetbanking.repository.UserRepository;
import com.example.internetbanking.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;
    
    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private JWTService jwtService;

    private Account mockAccount;
    private User mockUser;
    private RechargeRequest mockRechargeRequest;

    @BeforeEach
    void setUp() {
        mockAccount = new Account();
        mockAccount.setAccountNumber("1234567890");
        mockAccount.setCurrentBalance(new BigDecimal("1000.00"));
        mockAccount.setUserId("user123");
        mockAccount.setAccountType("Savings");
        mockAccount.setTpin(1234);

        mockUser = new User();
        mockUser.setUserId("user123");
        
        mockRechargeRequest = new RechargeRequest();
        mockRechargeRequest.setAccountNumber("1234567890");
        mockRechargeRequest.setTpin(1234);
        mockRechargeRequest.setAmount(new BigDecimal("100.00"));
    }

    @Test
    void testGetAccounts_Success() throws Exception {
        when(accountService.getUserAccounts("user123")).thenReturn(List.of(mockAccount));

        mockMvc.perform(get("/api/accounts/user/{userId}", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountNumber").value("1234567890"));
    }
    
    @Test
    void testGetAccountBalance_Success() throws Exception {
        when(accountService.getBalance("1234567890")).thenReturn(new BigDecimal("1000.00"));

        mockMvc.perform(get("/api/accounts/{accNo}/balance", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1000.00));
    }

    @Test
    void testGetAccountDetails_Success() throws Exception {
        when(accountService.getAccount("1234567890")).thenReturn(mockAccount);

        mockMvc.perform(get("/api/accounts/details/{accNo}", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"));
    }
    
    @Test
    void testGetTransactions_Success() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setUtrNumber("utr123"); 
        when(accountService.getTransactions("1234567890")).thenReturn(List.of(transaction));

        mockMvc.perform(get("/api/accounts/{accNo}/transactions", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].utrNumber").value("utr123")); 
    }

    @Test
    void testGetMiniStatement_Success() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setUtrNumber("utr123"); 
        when(accountService.getMiniStatement("1234567890")).thenReturn(List.of(transaction));

        mockMvc.perform(get("/api/accounts/{accNo}/mini", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].utrNumber").value("utr123"));
    }
    
  
    @Test
    void testCreateAccount_Success() throws Exception {
        when(userRepository.findByUserId("user123")).thenReturn(mockUser);
        when(accountService.createAccount1(anyString(), anyString(), any(BigDecimal.class), anyInt()))
                .thenReturn(mockAccount);

        mockMvc.perform(post("/api/accounts/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockAccount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"));
    }
    
    @Test
    void testCreateAccount_UserNotFound() throws Exception {
        when(userRepository.findByUserId("user123")).thenReturn(null);

        mockMvc.perform(post("/api/accounts/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockAccount)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testRechargeAccount_Success() throws Exception {
        when(accountService.rechargeAccount(anyString(), anyInt(), any(BigDecimal.class))).thenReturn(true);

        mockMvc.perform(post("/api/accounts/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRechargeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Recharge successful"));
    }

    @Test
    void testRechargeAccount_Failure() throws Exception {
        when(accountService.rechargeAccount(anyString(), anyInt(), any(BigDecimal.class))).thenReturn(false);

        mockMvc.perform(post("/api/accounts/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRechargeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Invalid TPIN or Account"));
    }
}