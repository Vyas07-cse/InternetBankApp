package com.example.internetbanking.service.impl;
import com.example.internetbanking.repository.TransactionRepository;
import com.example.internetbanking.entity.Account;
import com.example.internetbanking.entity.Transaction;
import com.example.internetbanking.repository.AccountRepository;
import com.example.internetbanking.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.internetbanking.repository.UserRepository;
import com.example.internetbanking.exception.AccountOperationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired 
    private TransactionRepository txnRepo;
    
    @Autowired 
    private UserRepository userRepository;
    
   
    
    public List<Account> getUserAccounts(String userId) {
    	if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User ID " + userId + " not found.");
        }
        return accountRepository.findByUserId(userId);
    }
    
    
    @Override
    public boolean rechargeAccount(String accountNumber, int tpin, BigDecimal amount) {
        Optional<Account> optionalAccount = accountRepository.findByAccountNumber(accountNumber);
        
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            if (account.getTpin() == tpin) {
                BigDecimal newBalance = account.getCurrentBalance().add(amount);
                account.setCurrentBalance(newBalance);
                accountRepository.save(account);
                return true;
            }
        }
        return false;
    }


    public List<Transaction> getTransactions(String accNo) {
    	accountRepository.findByAccountNumber(accNo)
        .orElseThrow(() -> new AccountOperationException("Account number not found: " + accNo));
        return txnRepo.findByFromAccount(accNo);
    }

    public List<Transaction> getMiniStatement(String accNo) {
    	accountRepository.findByAccountNumber(accNo)
        .orElseThrow(() -> new AccountOperationException("Account number not found: " + accNo));
        return txnRepo.findTop5ByFromAccountOrderByDateTimeDesc(accNo);
    }
    public BigDecimal getBalance(String acccNo) {
    	accountRepository.findByAccountNumber(acccNo)
        .orElseThrow(() -> new AccountOperationException("Account number not found: " + acccNo));
    	Optional<Account> account=accountRepository.findByAccountNumber(acccNo);
    	return account.get().getCurrentBalance();
    }
    public Account getAccount(String accNo) {
    	accountRepository.findByAccountNumber(accNo)
        .orElseThrow(() -> new AccountOperationException("Account number not found: " + accNo));
    	Optional<Account> acclist=accountRepository.findByAccountNumber(accNo);
    	Account account=acclist.get();
    	if(account==null)
    	{
    		throw new AccountOperationException("Account not found");
    	}
    	return account;
    }
    
    
    @Override
    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountOperationException("Account not found."));
    }
    
    @Override
    public Account createAccount1(String userId, String accountType, BigDecimal initialBalance,int tpin) {
        Account account = new Account();
        account.setUserId(userId);
        account.setAccountType(accountType);
        account.setCurrentBalance(initialBalance != null ? initialBalance : BigDecimal.ZERO);
        account.setCreatedAt(LocalDateTime.now());
        account.setTpin(tpin);
        String accountNumber;
        do {
            accountNumber = generateAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        account.setAccountNumber(accountNumber);

        return accountRepository.save(account);
    }
    
    private String generateAccountNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}