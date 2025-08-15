package com.example.internetbanking.repository;
import java.util.*;
import com.example.internetbanking.entity.BillPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillPaymentRepository extends JpaRepository<BillPayment, Long> {
	List<BillPayment> findByAccountNumber(String accountNumber);


}
