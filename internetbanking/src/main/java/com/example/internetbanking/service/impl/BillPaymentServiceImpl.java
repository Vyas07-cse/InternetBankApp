package com.example.internetbanking.service.impl;

import com.example.internetbanking.exception.BillPaymentException;
import com.example.internetbanking.entity.Account;
import com.example.internetbanking.entity.BillPayment;
import com.example.internetbanking.entity.Transaction;
import com.example.internetbanking.repository.AccountRepository;
import com.example.internetbanking.repository.BillPaymentRepository;
import com.example.internetbanking.repository.TransactionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.internetbanking.util.BillTypeValidator;
import com.example.internetbanking.exception.AccountOperationException;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.time.LocalDateTime;

@Service
public class BillPaymentServiceImpl  {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BillPaymentRepository billPaymentRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Transactional
    public String payBill(String accountNumber, String billType, double  billAmount,String serviceNumber) {
    	if (!BillTypeValidator.isValid(billType)) {
            throw new AccountOperationException("Invalid bill type. Valid types: " + BillTypeValidator.getValidTypes());
        }
        System.out.println("Received payment request for account: " + accountNumber);
       
            if (billAmount <= 0) {
                throw new AccountOperationException("Bill amount must be greater than zero");
            }

            Account account = accountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new BillPaymentException("Account not found for number: " + accountNumber));

            if (account.getCurrentBalance() < billAmount) {
                return "Insufficient balance to pay the bill.";
            }

        
        switch (billType.toLowerCase()) {
        case "electricity":
            if (!serviceNumber.startsWith("ELEC")) {
                throw new BillPaymentException("Electricity service numbers must start with 'ELEC'");
            }
            break;
        case "gas":
            if (!serviceNumber.startsWith("GAS")) {
                throw new BillPaymentException("Gas service numbers must start with 'GAS'");
            }
            break;
        case "water":
            if (!serviceNumber.startsWith("WAT")) {
                throw new BillPaymentException("Water service numbers must start with 'WAT'");
            }
        case "internet":
        {
        }
            break;
        default:
            throw new BillPaymentException("Invalid bill type. Allowed types: Electricity, Gas, Water");
    }

 
        account.setCurrentBalance(account.getCurrentBalance()-billAmount);
        accountRepository.save(account);
        System.out.println("Balance deducted. New balance: " + account.getCurrentBalance());

       
        BillPayment payment = new BillPayment();
        payment.setAccountNumber(accountNumber);
        payment.setBillType(billType);
        payment.setBillAmount(billAmount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setUtrNumber(generateUtrNumber());
        payment.setServiceNumber(serviceNumber);
        Transaction txn=new Transaction();
        txn.setFromAccount(accountNumber);
        txn.setId(transactionId());
        txn.setToAccount(serviceNumber);
        txn.setDateTime(LocalDateTime.now());
        txn.setStatus("Success");
        txn.setMode("Bills");
        txn.setUtrNumber(payment.getUtrNumber());
        txn.setAmount(billAmount);
        transactionRepository.save(txn);
        billPaymentRepository.save(payment);
        System.out.println("Bill payment saved. UTR: " + payment.getUtrNumber());

        return "Bill payment successful! UTR: " + payment.getUtrNumber();
    }


    private String generateUtrNumber() {
        return "UTR" + System.currentTimeMillis();
    }
    private String transactionId() {
    	return "TXN"+(int)(1000 + Math.random() * 9000);
    }
   
    public List<BillPayment> getBillPaymentHistory(String accountNumber) {
    	Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BillPaymentException("Account not found for number: " + accountNumber));
        return billPaymentRepository.findByAccountNumber(accountNumber);
    }
}
