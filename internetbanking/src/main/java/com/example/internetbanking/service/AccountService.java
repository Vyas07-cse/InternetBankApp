package com.example.internetbanking.service;

import com.example.internetbanking.entity.Account;
import com.example.internetbanking.entity.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    Account createAccount1(String userId, String accountType, BigDecimal initialBalance,int tpin);

    Account getAccountByNumber(String accountNumber);

    List<Account> getUserAccounts(String userId);

    List<Transaction> getTransactions(String accNo);

    List<Transaction> getMiniStatement(String accNo);

    BigDecimal getBalance(String accNo);

    Account getAccount(String accNo);

    boolean rechargeAccount(String accountNumber, int tpin, BigDecimal amount);

}
