package com.example.internetbanking.service;

import com.example.internetbanking.entity.Account;
import com.example.internetbanking.entity.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    Account createAccount1(String userId, String accountType, double initialBalance,int tpin);

    Account getAccountByNumber(String accountNumber);

    List<Account> getUserAccounts(String userId);

    List<Transaction> getTransactions(String accNo);

    List<Transaction> getMiniStatement(String accNo);

    double getBalance(String accNo);

    Account getAccount(String accNo);

    boolean rechargeAccount(String accountNumber, int tpin, double amount);

}
