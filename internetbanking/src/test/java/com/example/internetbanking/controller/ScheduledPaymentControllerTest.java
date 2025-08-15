package com.example.internetbanking.controller;

import com.example.internetbanking.service.impl.JWTService; 
import com.example.internetbanking.entity.ScheduledPayment;
import com.example.internetbanking.service.impl.ScheduledPaymentServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScheduledPaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class ScheduledPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScheduledPaymentServiceImpl scheduledPaymentService;

    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private JWTService jwtService;


    private ScheduledPayment payment;

    @BeforeEach
    void setUp() {
        payment = new ScheduledPayment();
        payment.setScheduleId(1L);
        payment.setAccountNumber("123456789012");
        payment.setBillType("Electricity");
        payment.setServiceNumber("ELEC123456");
        payment.setAmount(new BigDecimal("499.99"));
        payment.setScheduledDate(LocalDateTime.now().plusDays(3));
        payment.setStatus("Scheduled");
    }

    @Test
    void testScheduleBillPayment_Success() throws Exception {
        when(scheduledPaymentService.schedulePayment(Mockito.any(ScheduledPayment.class)))
                .thenReturn("Scheduled successfully");

        mockMvc.perform(post("/api/scheduled-payments/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment)))
                .andExpect(status().isOk())
                .andExpect(content().string("Scheduled successfully"));
    }

    @Test
    void testGetScheduledPayments_Success() throws Exception {
        List<ScheduledPayment> payments = List.of(payment);
        when(scheduledPaymentService.getScheduledPayments("123456789012"))
                .thenReturn(payments);

        mockMvc.perform(get("/api/scheduled-payments/list")
                .param("accountNumber", "123456789012"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountNumber").value("123456789012"))
                .andExpect(jsonPath("$[0].billType").value("Electricity"))
                .andExpect(jsonPath("$[0].amount").value(499.99))
                .andExpect(jsonPath("$[0].serviceNumber").value("ELEC123456"))
                .andExpect(jsonPath("$[0].status").value("Scheduled"));
    }

    @Test
    void testCancelScheduledPayment_Success() throws Exception {
        when(scheduledPaymentService.cancelScheduledPayment(1L, "123456789012"))
                .thenReturn("Cancelled successfully");

        mockMvc.perform(delete("/api/scheduled-payments/cancel")
                .param("scheduleId", "1")
                .param("accountNumber", "123456789012"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cancelled successfully"));
    }
}