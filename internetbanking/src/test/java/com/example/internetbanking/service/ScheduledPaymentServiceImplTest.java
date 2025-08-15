package com.example.internetbanking.service;
import com.example.internetbanking.service.impl.ScheduledPaymentServiceImpl;
import com.example.internetbanking.entity.ScheduledPayment;
import com.example.internetbanking.exception.AccountOperationException;
import com.example.internetbanking.repository.AccountRepository;
import com.example.internetbanking.repository.ScheduledPaymentRepository;
import com.example.internetbanking.repository.BillPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledPaymentServiceImplTest {

    @InjectMocks
    private ScheduledPaymentServiceImpl scheduledPaymentService;

    @Mock
    private ScheduledPaymentRepository scheduledPaymentRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BillPaymentRepository billPaymentRepository;

    private ScheduledPayment mockPayment;

    @BeforeEach
    void setUp() {
        mockPayment = new ScheduledPayment();
        mockPayment.setAccountNumber("1234567890");
        mockPayment.setBillType("Electricity");
        mockPayment.setAmount(new BigDecimal("500.00"));
        mockPayment.setScheduledDate(LocalDateTime.now().plusDays(1));
        mockPayment.setServiceNumber("ELEC98765");
    }

    @Test
    void testSchedulePayment_Success() {
        when(accountRepository.existsByAccountNumber("1234567890")).thenReturn(true);
        String result = scheduledPaymentService.schedulePayment(mockPayment);

        assertEquals("Payment scheduled successfully!", result);
        verify(scheduledPaymentRepository).save(mockPayment);
    }

    @Test
    void testSchedulePayment_DateInPast() {
        mockPayment.setScheduledDate(LocalDateTime.now().minusDays(1));

        AccountOperationException ex = assertThrows(AccountOperationException.class, () ->
                scheduledPaymentService.schedulePayment(mockPayment));

        assertEquals("Scheduled time must be in the future.", ex.getMessage());
    }

    @Test
    void testSchedulePayment_InvalidBillType() {
        mockPayment.setBillType("Invalid");

        AccountOperationException ex = assertThrows(AccountOperationException.class, () ->
                scheduledPaymentService.schedulePayment(mockPayment));

        assertTrue(ex.getMessage().contains("Invalid bill type"));
    }

    @Test
    void testSchedulePayment_InvalidServiceNumberFormat() {
        mockPayment.setServiceNumber("WRONG123");

        AccountOperationException ex = assertThrows(AccountOperationException.class, () ->
                scheduledPaymentService.schedulePayment(mockPayment));

        assertEquals("Electricity service numbers must start with 'ELEC'", ex.getMessage());
    }

    @Test
    void testSchedulePayment_ZeroAmount() {
        mockPayment.setAmount(BigDecimal.ZERO);

        AccountOperationException ex = assertThrows(AccountOperationException.class, () ->
                scheduledPaymentService.schedulePayment(mockPayment));

        assertEquals("Scheduled amount must be greater than 0", ex.getMessage());
    }

    @Test
    void testSchedulePayment_AccountNotFound() {
        when(accountRepository.existsByAccountNumber("1234567890")).thenReturn(false);

        AccountOperationException ex = assertThrows(AccountOperationException.class, () ->
                scheduledPaymentService.schedulePayment(mockPayment));

        assertEquals("Account number not found. Please provide a valid account.", ex.getMessage());
    }

    @Test
    void testGetScheduledPayments_Success() {
        List<ScheduledPayment> list = List.of(mockPayment);
        when(accountRepository.existsByAccountNumber("1234567890")).thenReturn(true);
        when(scheduledPaymentRepository.findByAccountNumber("1234567890")).thenReturn(list);

        List<ScheduledPayment> result = scheduledPaymentService.getScheduledPayments("1234567890");
        assertEquals(1, result.size());
    }

    @Test
    void testGetScheduledPayments_AccountNotFound() {
        when(accountRepository.existsByAccountNumber("1234567890")).thenReturn(false);

        assertThrows(AccountOperationException.class, () ->
                scheduledPaymentService.getScheduledPayments("1234567890"));
    }

    @Test
    void testCancelScheduledPayment_Success() {
        mockPayment.setScheduleId(1L);
        mockPayment.setStatus("Pending");
        when(scheduledPaymentRepository.findById(1L)).thenReturn(Optional.of(mockPayment));

        String result = scheduledPaymentService.cancelScheduledPayment(1L, "1234567890");

        assertEquals("Scheduled payment cancelled successfully!", result);
        verify(scheduledPaymentRepository).delete(mockPayment);
    }

    @Test
    void testCancelScheduledPayment_NotFound() {
        when(scheduledPaymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AccountOperationException.class, () ->
                scheduledPaymentService.cancelScheduledPayment(1L, "1234567890"));
    }

    @Test
    void testCancelScheduledPayment_AccountMismatch() {
        mockPayment.setScheduleId(1L);
        mockPayment.setAccountNumber("9999999999");
        when(scheduledPaymentRepository.findById(1L)).thenReturn(Optional.of(mockPayment));

        assertThrows(AccountOperationException.class, () ->
                scheduledPaymentService.cancelScheduledPayment(1L, "1234567890"));
    }
}
