package com.example.internetbanking.service;

import com.example.internetbanking.entity.Account;
import com.example.internetbanking.entity.BillPayment;
import com.example.internetbanking.exception.AccountOperationException;
import com.example.internetbanking.exception.BillPaymentException;
import com.example.internetbanking.repository.AccountRepository;
import com.example.internetbanking.repository.BillPaymentRepository;
import com.example.internetbanking.service.impl.BillPaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillPaymentServiceImplTest {

    @InjectMocks
    private BillPaymentServiceImpl billPaymentService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BillPaymentRepository billPaymentRepository;

    private Account mockAccount;

    @BeforeEach
    void setUp() {
        mockAccount = new Account();
        mockAccount.setAccountNumber("1234567890");
        mockAccount.setCurrentBalance(1000.00);
        mockAccount.setTpin(1234);
    }

    @Test
    void testPayBill_Success() {
        when(accountRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.of(mockAccount));

        String result = billPaymentService.payBill("1234567890", "Electricity", 500.00, "ELEC12345");

        assertTrue(result.contains("Bill payment successful! UTR:"));
        verify(accountRepository, times(1)).save(any(Account.class));
        verify(billPaymentRepository, times(1)).save(any(BillPayment.class));
    }

    @Test
    void testPayBill_InsufficientBalance() {
        mockAccount.setCurrentBalance(100.00);
        when(accountRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.of(mockAccount));

        String result = billPaymentService.payBill("1234567890", "Electricity", 500.00, "ELEC12345");

        assertEquals("Insufficient balance to pay the bill.", result);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testPayBill_InvalidBillType() {
        Exception ex = assertThrows(AccountOperationException.class, () ->
                billPaymentService.payBill("1234567890", "InvalidType", 100.00, "ELEC12345"));

        assertTrue(ex.getMessage().contains("Invalid bill type"));
    }

    @Test
    void testPayBill_InvalidServiceNumberFormat() {
        when(accountRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.of(mockAccount));

        Exception ex = assertThrows(BillPaymentException.class, () ->
                billPaymentService.payBill("1234567890", "Electricity", 100.00, "WRONG123"));

        assertEquals("Electricity service numbers must start with 'ELEC'", ex.getMessage());
    }

    @Test
    void testGetBillPaymentHistory_Success() {
        List<BillPayment> payments = new ArrayList<>();
        payments.add(new BillPayment());

        when(accountRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.of(mockAccount));
        when(billPaymentRepository.findByAccountNumber("1234567890"))
                .thenReturn(payments);

        List<BillPayment> result = billPaymentService.getBillPaymentHistory("1234567890");
        assertEquals(1, result.size());
    }

    @Test
    void testGetBillPaymentHistory_AccountNotFound() {
        when(accountRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.empty());

        assertThrows(BillPaymentException.class, () ->
                billPaymentService.getBillPaymentHistory("1234567890"));
    }

    @Test
    void testPayBill_InvalidAmount() {
        Exception ex = assertThrows(AccountOperationException.class, () ->
                billPaymentService.payBill("1234567890", "Electricity", 0.00, "ELEC12345"));

        assertEquals("BillAmount must be greater than Zero", ex.getMessage());
    }
}
