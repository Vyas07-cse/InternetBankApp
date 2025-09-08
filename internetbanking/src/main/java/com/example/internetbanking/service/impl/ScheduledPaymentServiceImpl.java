package com.example.internetbanking.service.impl;
import com.example.internetbanking.entity.ScheduledPayment;
import com.example.internetbanking.repository.ScheduledPaymentRepository;
import com.example.internetbanking.service.ScheduledPaymentService;
import com.example.internetbanking.util.BillTypeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.internetbanking.exception.AccountOperationException;
import com.example.internetbanking.repository.AccountRepository;
import java.util.*;
import java.time.LocalDateTime;


@Service
public class ScheduledPaymentServiceImpl implements ScheduledPaymentService {

    @Autowired
    private ScheduledPaymentRepository scheduledPaymentRepository;
    
    @Autowired
    private AccountRepository accountRepository;

    @Override
    public String schedulePayment(ScheduledPayment payment) {
    	if (payment.getScheduledDate().isBefore(LocalDateTime.now())) {
            throw new AccountOperationException("Scheduled time must be in the future.");
        }
    	if (!BillTypeValidator.isValid(payment.getBillType())) {
            throw new AccountOperationException("Invalid bill type. Allowed types: " + BillTypeValidator.getValidTypes());
        }
    	switch (payment.getBillType().toLowerCase()) {
        case "electricity":
            if (!payment.getServiceNumber().startsWith("ELEC")) {
                throw new AccountOperationException("Electricity service numbers must start with 'ELEC'");
            }
            break;
        case "gas":
            if (!payment.getServiceNumber().startsWith("GAS")) {
                throw new AccountOperationException("Gas service numbers must start with 'GAS'");
            }
            break;
        case "water":
            if (!payment.getServiceNumber().startsWith("WAT")) {
                throw new AccountOperationException("Water service numbers must start with 'WAT'");
            }
            break;
        case "internet":
        {
        	
        }
        break;
        default:
            throw new AccountOperationException("Invalid bill type. Allowed types: Electricity, Gas, Water");
    }
    	if (payment.getAmount() == 0 || payment.getAmount() <= 0) {
            throw new AccountOperationException("Scheduled amount must be greater than 0");
        }
    	
    	
    	boolean exists = accountRepository.existsByAccountNumber(payment.getAccountNumber());
        if (!exists) {
            throw new AccountOperationException("Account number not found. Please provide a valid account.");
        }
        payment.setStatus("Pending");
        scheduledPaymentRepository.save(payment);
        return "Payment scheduled successfully!";
    }

    @Override
    public List<ScheduledPayment> getScheduledPayments(String accountNumber) {
    	boolean exists = accountRepository.existsByAccountNumber(accountNumber);
        if (!exists) {
            throw new AccountOperationException("Account number not found. Please provide a valid account.");
        }
        return scheduledPaymentRepository.findByAccountNumber(accountNumber);
    }

    @Override
    public String cancelScheduledPayment(Long scheduleId, String accountNumber) {
        ScheduledPayment payment = scheduledPaymentRepository.findById(scheduleId)
            .orElseThrow(() -> new AccountOperationException("Scheduled Payment with this Id not found"));

        if (!payment.getAccountNumber().equals(accountNumber)) {
            throw new AccountOperationException("Account number does not match scheduled payment owner.");
        }
        if (!"Pending".equalsIgnoreCase(payment.getStatus())) {
            throw new AccountOperationException("Only pending or payments can be cancelled.");
        }

        
        scheduledPaymentRepository.delete(payment);

        return "Scheduled payment cancelled successfully!";
    }
    
}
