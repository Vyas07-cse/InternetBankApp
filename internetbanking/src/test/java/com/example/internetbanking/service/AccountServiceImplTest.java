package com.example.internetbanking.service;
import com.example.internetbanking.service.impl.AccountServiceImpl;
import com.example.internetbanking.entity.Account;
import com.example.internetbanking.entity.Transaction;
import com.example.internetbanking.exception.AccountOperationException;
import com.example.internetbanking.repository.AccountRepository;
import com.example.internetbanking.repository.TransactionRepository;
import com.example.internetbanking.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceImplTest {

    @InjectMocks
    private AccountServiceImpl accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository txnRepo;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserAccounts_UserExists_ReturnsAccounts() {
        String userId = "user123";
        List<Account> accounts = List.of(new Account(), new Account());

        when(userRepository.existsById(userId)).thenReturn(true);
        when(accountRepository.findByUserId(userId)).thenReturn(accounts);

        List<Account> result = accountService.getUserAccounts(userId);

        assertEquals(2, result.size());
        verify(userRepository).existsById(userId);
        verify(accountRepository).findByUserId(userId);
    }

    @Test
    void testGetUserAccounts_UserNotExists_ThrowsException() {
        String userId = "user123";

        when(userRepository.existsById(userId)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountService.getUserAccounts(userId));
        assertTrue(ex.getMessage().contains(userId));
        verify(userRepository).existsById(userId);
        verify(accountRepository, never()).findByUserId(any());
    }

    @Test
    void testRechargeAccount_Success() {
        String accNo = "123456789012";
        int tpin = 1234;
        BigDecimal amount = BigDecimal.valueOf(1000);

        Account account = new Account();
        account.setAccountNumber(accNo);
        account.setTpin(tpin);
        account.setCurrentBalance(BigDecimal.valueOf(2000));

        when(accountRepository.findByAccountNumber(accNo)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        boolean result = accountService.rechargeAccount(accNo, tpin, amount);

        assertTrue(result);
        assertEquals(BigDecimal.valueOf(3000), account.getCurrentBalance());
        verify(accountRepository).save(account);
    }

    @Test
    void testRechargeAccount_WrongTpin_ReturnsFalse() {
        String accNo = "123456789012";
        int tpin = 1111;
        BigDecimal amount = BigDecimal.valueOf(1000);

        Account account = new Account();
        account.setAccountNumber(accNo);
        account.setTpin(1234);
        account.setCurrentBalance(BigDecimal.valueOf(2000));

        when(accountRepository.findByAccountNumber(accNo)).thenReturn(Optional.of(account));

        boolean result = accountService.rechargeAccount(accNo, tpin, amount);

        assertFalse(result);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testGetTransactions_AccountExists_ReturnsTransactions() {
        String accNo = "123456789012";
        Account account = new Account();
        account.setAccountNumber(accNo);

        List<Transaction> transactions = List.of(new Transaction(), new Transaction());

        when(accountRepository.findByAccountNumber(accNo)).thenReturn(Optional.of(account));
        when(txnRepo.findByFromAccount(accNo)).thenReturn(transactions);

        List<Transaction> result = accountService.getTransactions(accNo);

        assertEquals(2, result.size());
    }

    @Test
    void testGetTransactions_AccountNotExists_ThrowsException() {
        String accNo = "123456789012";

        when(accountRepository.findByAccountNumber(accNo)).thenReturn(Optional.empty());

        assertThrows(AccountOperationException.class, () -> accountService.getTransactions(accNo));
    }

    @Test
    void testGetMiniStatement_AccountExists_ReturnsTop5Transactions() {
        String accNo = "123456789012";
        Account account = new Account();
        account.setAccountNumber(accNo);

        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            transactions.add(new Transaction());
        }

        when(accountRepository.findByAccountNumber(accNo)).thenReturn(Optional.of(account));
        when(txnRepo.findTop5ByFromAccountOrderByDateTimeDesc(accNo)).thenReturn(transactions);

        List<Transaction> result = accountService.getMiniStatement(accNo);

        assertEquals(5, result.size());
    }

    @Test
    void testGetBalance_AccountExists_ReturnsBalance() {
        String accNo = "123456789012";
        Account account = new Account();
        account.setAccountNumber(accNo);
        account.setCurrentBalance(BigDecimal.valueOf(5000));

        when(accountRepository.findByAccountNumber(accNo)).thenReturn(Optional.of(account));

        BigDecimal balance = accountService.getBalance(accNo);

        assertEquals(BigDecimal.valueOf(5000), balance);
    }

    @Test
    void testGetBalance_AccountNotExists_ThrowsException() {
        String accNo = "123456789012";

        when(accountRepository.findByAccountNumber(accNo)).thenReturn(Optional.empty());

        assertThrows(AccountOperationException.class, () -> accountService.getBalance(accNo));
    }

    @Test
    void testCreateAccount1_Success() {
        String userId = "user123";
        String accountType = "Savings";
        BigDecimal initialBalance = BigDecimal.valueOf(1000);
        int tpin = 1234;

        Account savedAccount = new Account();
        savedAccount.setUserId(userId);
        savedAccount.setAccountType(accountType);
        savedAccount.setCurrentBalance(initialBalance);
        savedAccount.setTpin(tpin);
        savedAccount.setAccountNumber("123456789012");
        savedAccount.setCreatedAt(LocalDateTime.now());

        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        Account result = accountService.createAccount1(userId, accountType, initialBalance, tpin);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(accountType, result.getAccountType());
        assertEquals(initialBalance, result.getCurrentBalance());
        assertEquals(tpin, result.getTpin());
        assertEquals(12, result.getAccountNumber().length());
        verify(accountRepository, times(1)).save(any(Account.class));
    }
}
