package com.example.internetbanking.repository;

import com.example.internetbanking.entity.ScheduledPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledPaymentRepository extends JpaRepository<ScheduledPayment, Long> {
    List<ScheduledPayment> findByAccountNumber(String accountNumber);
    List<ScheduledPayment> findByStatusAndScheduledDateBefore(String status, LocalDateTime scheduledDate);

}
