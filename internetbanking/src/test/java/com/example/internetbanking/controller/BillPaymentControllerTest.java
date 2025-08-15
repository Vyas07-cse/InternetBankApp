package com.example.internetbanking.controller;

import com.example.internetbanking.dto.PayBillRequest;
import com.example.internetbanking.entity.BillPayment;
import com.example.internetbanking.exception.BillPaymentException;
import com.example.internetbanking.service.impl.JWTService; 
import com.example.internetbanking.service.impl.BillPaymentServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; 
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BillPaymentController.class)
@AutoConfigureMockMvc(addFilters = false) 
public class BillPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillPaymentServiceImpl billPaymentService;
    
    @MockBean
    private JWTService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private PayBillRequest request;
    private List<BillPayment> sampleHistory;

    @BeforeEach
    void setUp() {
        request = new PayBillRequest();
        request.setAccountNumber("1234567890");
        request.setBillAmount(new BigDecimal("250.00"));
        request.setBillType("Electricity");
        request.setServiceNumber("ELEC1234");

        BillPayment payment = new BillPayment();
        payment.setBillType("Electricity");
        payment.setBillAmount(new BigDecimal("250.00"));
        payment.setServiceNumber("ELEC1234");
        payment.setAccountNumber("1234567890");

        sampleHistory = new ArrayList<>();
        sampleHistory.add(payment);
    }

    @Test
    void testPayBill_Success() throws Exception {
        when(billPaymentService.payBill(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(BigDecimal.class),
                ArgumentMatchers.anyString()))
                .thenReturn("Bill paid successfully");

        mockMvc.perform(post("/api/bills/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Bill paid successfully"));
    }

    @Test
    void testGetBillHistory_Success() throws Exception {
        when(billPaymentService.getBillPaymentHistory("1234567890"))
                .thenReturn(sampleHistory);

        mockMvc.perform(get("/api/bills/history")
                .param("accountNumber", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].billType").value("Electricity"))
                .andExpect(jsonPath("$[0].billAmount").value(250.00))
                .andExpect(jsonPath("$[0].serviceNumber").value("ELEC1234"))
                .andExpect(jsonPath("$[0].accountNumber").value("1234567890"));
    }

    @Test
    void testGetBillHistory_NoData() throws Exception {
        when(billPaymentService.getBillPaymentHistory("1234567890"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/bills/history")
                .param("accountNumber", "1234567890"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No payments found"));
    }

    @Test
    void testPayBill_BadRequest() throws Exception {
        when(billPaymentService.payBill(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(BigDecimal.class),
                ArgumentMatchers.anyString()))
                .thenThrow(new BillPaymentException("Invalid bill details"));

        mockMvc.perform(post("/api/bills/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid bill details"));
    }
}