package com.example.internetbanking.controller;
import com.example.internetbanking.dto.RechargeRequest;
import com.example.internetbanking.entity.Account;
import com.example.internetbanking.entity.User;
import com.example.internetbanking.entity.Transaction;
import com.example.internetbanking.repository.AccountRepository;
import com.example.internetbanking.exception.AccountOperationException;
import com.example.internetbanking.service.AccountService;
import com.example.internetbanking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.*;
import org.springframework.validation.annotation.Validated;
import java.math.BigDecimal;
import java.util.*;

@RestController
@Validated
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired 
    UserRepository userRepository;
    
    @GetMapping("/user/{userId}")
    public List<Account> getAccounts(@PathVariable  String userId) {
        return accountService.getUserAccounts(userId);
    }
   
    @GetMapping("/balance/{accNo}")
    public ResponseEntity<Double> getAccountBalance(@PathVariable String accNo) {
        return ResponseEntity.ok(accountService.getBalance(accNo));
    }
    @GetMapping("/details/{accNo}")
    public ResponseEntity<Account> getAccount(@PathVariable String accNo) {
    	return ResponseEntity.ok(accountService.getAccount(accNo));    }

    @GetMapping("/{accNo}/transactions")
    public List<Transaction> getTransactions(@PathVariable String accNo) {
        return accountService.getTransactions(accNo);
    }
    
    @GetMapping("/mini/{accNo}")
    public List<Transaction> getMiniStatement(@PathVariable String accNo) {
        return accountService.getMiniStatement(accNo);
    }
 
    
    @PostMapping("/create")
    public ResponseEntity<Account> createAccountWithBalance(@Valid @RequestBody Account account) {

        String userId = account.getUserId();
        String accountType = account.getAccountType();
        double initialBalance = account.getCurrentBalance();
        int tpin = account.getTpin();

        User userOpt = userRepository.findByUserId(userId);
        if (userOpt==null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Account createdAccount = accountService.createAccount1(userId, accountType, initialBalance, tpin);
        return ResponseEntity.ok(createdAccount);
    }
    
    
    @PostMapping("/recharge")
    public ResponseEntity<String> rechargeAccount(@Valid @RequestBody RechargeRequest request) {
        boolean success = accountService.rechargeAccount(
            request.getAccountNumber(), request.getTpin(), request.getAmount());

        if (success) {
            return ResponseEntity.ok("Recharge successful");
        } else {
        	throw new AccountOperationException("Invalid TPIN or Account");
        }
    }

}