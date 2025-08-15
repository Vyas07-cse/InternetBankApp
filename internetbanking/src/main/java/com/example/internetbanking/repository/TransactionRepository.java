package com.example.internetbanking.repository;

import com.example.internetbanking.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
	List<Transaction> findByFromAccount(String fromAccount);
    List<Transaction> findTop5ByFromAccountOrderByDateTimeDesc(String fromAccount);
	List<Transaction> findByStatusAndScheduledDateBefore(String string, LocalDateTime now);

}