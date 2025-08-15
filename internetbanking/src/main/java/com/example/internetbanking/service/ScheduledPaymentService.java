package com.example.internetbanking.service;

import com.example.internetbanking.entity.ScheduledPayment;
import java.util.List;

public interface ScheduledPaymentService {
    String schedulePayment(ScheduledPayment payment);
    List<ScheduledPayment> getScheduledPayments(String accountNumber);
    String cancelScheduledPayment(Long scheduleId,String accountNumber);
}
