package com.example.internetbanking.controller;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import com.example.internetbanking.dto.FundTransferRequest;
import com.example.internetbanking.dto.FundTransferResponse;
import com.example.internetbanking.dto.OtpVerifyRequest;
import com.example.internetbanking.entity.Beneficiary;
import com.example.internetbanking.repository.BeneficiaryRepository;
import com.example.internetbanking.repository.UserRepository;
import com.example.internetbanking.service.FundTransferService;
import com.example.internetbanking.service.impl.JWTService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.doNothing;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FundTransferController.class)
@AutoConfigureMockMvc(addFilters = false) 
class FundTransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private FundTransferService fundTransferService;

    @MockBean
    private BeneficiaryRepository beneficiaryRepo;

    @MockBean
    private JWTService jwtService;
    

    @MockBean
    private JavaMailSender mailSender;

    @MockBean
    private UserRepository userRepository;

    private Beneficiary beneficiary;

    @BeforeEach
    void setUp() {
        beneficiary = new Beneficiary();
        beneficiary.setBeneficiaryId("B001");
        beneficiary.setName("John Doe");
        beneficiary.setAccountNumber("1234567890");
        beneficiary.setBankName("Test Bank");
        beneficiary.setIfsc("TEST0123456");
        beneficiary.setEmail("test@example.com");
        beneficiary.setUserId("U001");
        beneficiary.setVerified(false);
        
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testTransfer() throws Exception {
        FundTransferRequest request = new FundTransferRequest();
        request.setFromAccount("1234567890");
        request.setToAccount("0987654321");
        request.setAmount(500.00);
        request.setTransactionType("TRANSFER"); 
        request.setTransferType("NEFT"); 
        request.setTpin(1234);

        FundTransferResponse mockResponse =
            new FundTransferResponse("completed", "Transfer successful", "UTR123456");

        when(fundTransferService.transfer(any(FundTransferRequest.class)))
            .thenReturn(mockResponse);

        mockMvc.perform(post("/api/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Transfer successful")));
    }



 
   
    @Test
    void testAddBeneficiary() throws Exception {
    	Beneficiary beneficiary = new Beneficiary();
        beneficiary.setUserId("user1");
        beneficiary.setBeneficiaryId("B001");
        beneficiary.setName("John Doe");
        beneficiary.setAccountNumber("1234567890");
        beneficiary.setBankName("Test Bank");
        beneficiary.setIfsc("TEST0123456");
        beneficiary.setEmail("test@example.com");
        beneficiary.setVerified(false);

        when(userRepository.existsById(anyString())).thenReturn(true);
        when(fundTransferService.addBeneficiary(any(Beneficiary.class)))
            .thenReturn("OTP sent for verification for Id bny1001");

        mockMvc.perform(post("/api/transfer/beneficiaries/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beneficiary)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("OTP sent for verification")));
    }


    @Test
    void testVerifyOtp() throws Exception {
        OtpVerifyRequest otpRequest = new OtpVerifyRequest();
        otpRequest.setBeneficiaryId("B001");
        otpRequest.setOtp("123456");

        when(fundTransferService.verifyOtp("B001", "123456")).thenReturn("OTP Verified");

        mockMvc.perform(post("/api/transfer/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otpRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP Verified"));
    }

    @Test
    void testGetTxnStatus() throws Exception {
        when(fundTransferService.getTxnStatus("srk")).thenReturn("COMPLETED");

        mockMvc.perform(get("/api/transfer/status/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("COMPLETED"));
    }
}
