package com.example.internetbanking.service;
import com.example.internetbanking.service.impl.FundTransferServiceImpl;
import com.example.internetbanking.dto.FundTransferRequest;
import com.example.internetbanking.dto.FundTransferResponse;
import com.example.internetbanking.entity.Account;
import com.example.internetbanking.entity.Beneficiary;
import com.example.internetbanking.entity.Transaction;
import com.example.internetbanking.exception.BeneficiaryFoundException;
import com.example.internetbanking.exception.CustomException;
import com.example.internetbanking.repository.AccountRepository;
import com.example.internetbanking.repository.BeneficiaryRepository;
import com.example.internetbanking.repository.TransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FundTransferServiceImplTest {

    @InjectMocks
    private FundTransferServiceImpl fundTransferService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private TransactionRepository transactionRepo;

    @Mock
    private BeneficiaryRepository beneficiaryRepo;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testTransfer_IntraBank_Success() {
        
        Account fromAccount = new Account();
        fromAccount.setAccountNumber("123456789012");
        fromAccount.setTpin(1234);
        fromAccount.setCurrentBalance(BigDecimal.valueOf(1000));

        
        Account toAccount = new Account();
        toAccount.setAccountNumber("098765432109");
        toAccount.setCurrentBalance(BigDecimal.valueOf(500));

        when(accountRepo.findByAccountNumber("123456789012")).thenReturn(Optional.of(fromAccount));
        when(accountRepo.findByAccountNumber("098765432109")).thenReturn(Optional.of(toAccount));
        when(accountRepo.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        FundTransferRequest request = new FundTransferRequest();
        request.setFromAccount("123456789012");
        request.setToAccount("098765432109");
        request.setAmount(200);
        request.setTpin(1234);
        request.setTransferType("IMPS");

        FundTransferResponse response = fundTransferService.transfer(request);

        assertEquals("completed", response.getStatus());
        assertEquals("success", response.getMessage());
        assertNotNull(response.getUtrNumber());

        verify(accountRepo, times(2)).save(any(Account.class));
        verify(transactionRepo, times(1)).save(any(Transaction.class));
    }

    @Test
    void testAddBeneficiary_Success() throws BeneficiaryFoundException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setAccountNumber("123");
        beneficiary.setUserId("user1");
        beneficiary.setEmail("test@example.com");

        when(beneficiaryRepo.existsByAccountNumberAndUserId("123", "user1")).thenReturn(false);
        when(beneficiaryRepo.count()).thenReturn(5L);
        when(beneficiaryRepo.save(any(Beneficiary.class))).thenAnswer(i -> i.getArgument(0));

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        String result = fundTransferService.addBeneficiary(beneficiary);

        assertTrue(result.contains("OTP sent for verification"));
        assertNotNull(beneficiary.getOtp());
        assertEquals(false, beneficiary.getVerified());
        verify(beneficiaryRepo, times(1)).save(beneficiary);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testVerifyOtp_Success() {
        Beneficiary ben = new Beneficiary();
        ben.setBeneficiaryId("bny1001");
        ben.setOtp("1234");
        ben.setVerified(false);

        when(beneficiaryRepo.findById("bny1001")).thenReturn(Optional.of(ben));
        when(beneficiaryRepo.save(any(Beneficiary.class))).thenAnswer(i -> i.getArgument(0));

        String result = fundTransferService.verifyOtp("bny1001", "1234");

        assertEquals("Beneficiary verified successfully!", result);
        assertTrue(ben.getVerified());
        assertNull(ben.getOtp());

        verify(beneficiaryRepo, times(1)).save(ben);
    }

    @Test
    void testVerifyOtp_InvalidOtp_Throws() {
        Beneficiary ben = new Beneficiary();
        ben.setBeneficiaryId("bny1001");
        ben.setOtp("1234");
        ben.setVerified(false);

        when(beneficiaryRepo.findById("bny1001")).thenReturn(Optional.of(ben));

        CustomException ex = assertThrows(CustomException.class, () -> 
            fundTransferService.verifyOtp("bny1001", "9999"));

        assertEquals("Invalid OTP", ex.getMessage());
    }

    @Test
    void testSendOtp_CallsMailSender() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        fundTransferService.sendOtp("test@example.com", "5678");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
