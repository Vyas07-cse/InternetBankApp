package com.example.internetbanking.service;

import com.example.internetbanking.dto.FundTransferRequest;
import com.example.internetbanking.dto.FundTransferResponse;
import com.example.internetbanking.entity.Account;
import com.example.internetbanking.entity.User;
import com.example.internetbanking.entity.Beneficiary;
import com.example.internetbanking.entity.Transaction;
import com.example.internetbanking.exception.BeneficiaryFoundException;
import com.example.internetbanking.exception.CustomException;
import com.example.internetbanking.repository.*;
import com.example.internetbanking.service.impl.FundTransferServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class FundTransferServiceImplTest {

    @InjectMocks
    private FundTransferServiceImpl fundTransferService;

    @Mock private JavaMailSender mailSender;
    @Mock private AccountRepository accountRepo;
    @Mock private UserRepository userRepo;
    @Mock private TransactionRepository transactionRepo;
    @Mock private BeneficiaryRepository beneficiaryRepo;

    private Account senderAccount;
    private Account receiverAccount;

    @BeforeEach
    void setup() {
        senderAccount = new Account();
        senderAccount.setAccountNumber("12345");
        senderAccount.setCurrentBalance(10000.0);
        senderAccount.setTpin(1111);
        senderAccount.setUserId("user1");

        receiverAccount = new Account();
        receiverAccount.setAccountNumber("67890");
        receiverAccount.setCurrentBalance(5000.0);
        receiverAccount.setUserId("user2");
    }

    @Test
    void testIntraBankTransferSuccess() {
        FundTransferRequest request = new FundTransferRequest();
        request.setFromAccount("12345");
        request.setToAccount("67890");
        request.setAmount(2000.0);
        request.setTransferType("IMPS");
        request.setTpin(1111);

        when(accountRepo.findByAccountNumber("12345")).thenReturn(Optional.of(senderAccount));
        when(accountRepo.findByAccountNumber("67890")).thenReturn(Optional.of(receiverAccount));
        when(userRepo.findByUserId("user1")).thenReturn(new User());

        FundTransferResponse response = fundTransferService.transfer(request);

        assertEquals("completed", response.getStatus());
        assertEquals("success", response.getMessage());
        verify(accountRepo, times(2)).save(any(Account.class));
        verify(transactionRepo, times(1)).save(any(Transaction.class));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testInvalidTpinThrowsException() {
        FundTransferRequest request = new FundTransferRequest();
        request.setFromAccount("12345");
        request.setToAccount("67890");
        request.setAmount(1000.0);
        request.setTransferType("NEFT");
        request.setTpin(2222);

        when(accountRepo.findByAccountNumber("12345")).thenReturn(Optional.of(senderAccount));

        CustomException ex = assertThrows(CustomException.class, () -> fundTransferService.transfer(request));
        assertEquals("Invalid TPIN", ex.getMessage());
    }

    @Test
    void testInsufficientBalanceThrowsException() {
        senderAccount.setCurrentBalance(500.0); // less than transfer amount

        FundTransferRequest request = new FundTransferRequest();
        request.setFromAccount("12345");
        request.setToAccount("67890");
        request.setAmount(2000.0);
        request.setTransferType("IMPS");
        request.setTpin(1111);

        when(accountRepo.findByAccountNumber("12345")).thenReturn(Optional.of(senderAccount));

        CustomException ex = assertThrows(CustomException.class, () -> fundTransferService.transfer(request));
        assertEquals("Insufficient balance", ex.getMessage());
    }


    @Test
    void testAddBeneficiaryAlreadyExists() {
        Beneficiary ben = new Beneficiary();
        ben.setAccountNumber("67890");
        ben.setUserId("user1");

        when(beneficiaryRepo.existsByAccountNumberAndUserId("67890", "user1")).thenReturn(true);

        assertThrows(BeneficiaryFoundException.class, () -> fundTransferService.addBeneficiary(ben));
    }

    @Test
    void testVerifyOtpSuccess() {
        Beneficiary ben = new Beneficiary();
        ben.setBeneficiaryId("bny1001");
        ben.setOtp("1234");

        when(beneficiaryRepo.findById("bny1001")).thenReturn(Optional.of(ben));

        String result = fundTransferService.verifyOtp("bny1001", "1234");

        assertEquals("Beneficiary verified successfully!", result);
        assertNull(ben.getOtp());
        assertTrue(ben.getVerified());
        verify(beneficiaryRepo, times(1)).save(ben);
    }

    @Test
    void testVerifyOtpFailure() {
        Beneficiary ben = new Beneficiary();
        ben.setBeneficiaryId("bny1001");
        ben.setOtp("1234");

        when(beneficiaryRepo.findById("bny1001")).thenReturn(Optional.of(ben));

        CustomException ex = assertThrows(CustomException.class, () -> fundTransferService.verifyOtp("bny1001", "9999"));
        assertEquals("Invalid OTP", ex.getMessage());
    }
}
