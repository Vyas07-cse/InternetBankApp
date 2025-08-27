package com.example.internetbanking.repository;

import com.example.internetbanking.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
	@Query("SELECT t FROM Transaction t WHERE t.fromAccount = :account OR t.toAccount = :account ORDER BY t.dateTime DESC")
	List<Transaction> findByAccount(@Param("account") String account);
    List<Transaction> findTop5ByFromAccountOrderByDateTimeDesc(String fromAccount);
	List<Transaction> findByStatusAndScheduledDateBefore(String string, LocalDateTime now);

}