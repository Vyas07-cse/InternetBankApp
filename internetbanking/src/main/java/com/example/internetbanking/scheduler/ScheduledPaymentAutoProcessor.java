package com.example.internetbanking.scheduler;
import com.example.internetbanking.entity.*;
import com.example.internetbanking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender; 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.print.attribute.standard.DateTimeAtCompleted;

@Component
public class ScheduledPaymentAutoProcessor {

    @Autowired
    private ScheduledPaymentRepository scheduledPaymentRepository;

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TransactionRepository transactionRepo;

    @Autowired
    private BillPaymentRepository billPaymentRepository;
    
    @Autowired
    private UserRepository userRepo;
    
    @Transactional
    @Scheduled(fixedRate = 300000)
    public void processScheduledPayments() {
        System.out.println("=== Scheduler Triggered at " + LocalDateTime.now() + " ===");

        List<ScheduledPayment> pending = scheduledPaymentRepository
                .findByStatusAndScheduledDateBefore("Pending", LocalDateTime.now());

        System.out.println("Pending scheduled payments found: " + pending.size());

        for (ScheduledPayment payment : pending) {
            Optional<Account> accountOpt = accountRepository.findByAccountNumber(payment.getAccountNumber());
            if (accountOpt.isPresent()) {
                Account account = accountOpt.get();
                BigDecimal amount = payment.getAmount();

                if (account.getCurrentBalance().compareTo(amount) >= 0) {
                    account.setCurrentBalance(account.getCurrentBalance().subtract(amount));
                    accountRepository.save(account);

                    BillPayment billPayment = new BillPayment();
                    billPayment.setAccountNumber(payment.getAccountNumber());
                    billPayment.setBillType(payment.getBillType());
                    billPayment.setBillAmount(amount);
                    billPayment.setServiceNumber(payment.getServiceNumber());
                    billPayment.setPaymentDate(LocalDateTime.now());
                    billPayment.setUtrNumber("UTR" + System.currentTimeMillis());
                    billPaymentRepository.save(billPayment);

                    payment.setStatus("Completed");
                    scheduledPaymentRepository.save(payment);

                    System.out.println("Payment completed for account: " + payment.getAccountNumber());
                } else {
                    System.out.println("Insufficient balance for account: " + payment.getAccountNumber());
                }
            }
        }
    }
    
    private String generateUtrNumber() {
        return "UTR" + System.currentTimeMillis();
    }

    
    @Transactional
    @Scheduled(fixedRate = 60000)  
    public void processScheduledTransfers() {
        LocalDateTime now = LocalDateTime.now();
        List<Transaction> pendingScheduledTxns = transactionRepo.findByStatusAndScheduledDateBefore("PENDING", now);

        for (Transaction txn : pendingScheduledTxns) {
            Optional<Account> fromOpt = accountRepository.findByAccountNumber(txn.getFromAccount());
            Optional<Account> toOpt = accountRepository.findByAccountNumber(txn.getToAccount());

            if (fromOpt.isEmpty()) {
                txn.setStatus("FAILED");
                transactionRepo.save(txn);
                continue;
            }

            Account fromAccount = fromOpt.get();

            if (fromAccount.getCurrentBalance().compareTo(BigDecimal.valueOf(txn.getAmount())) < 0) {
                txn.setStatus("FAILED");
                transactionRepo.save(txn);
                continue;
            }

            fromAccount.setCurrentBalance(
            	    fromAccount.getCurrentBalance().subtract(BigDecimal.valueOf(txn.getAmount()))
            	);
            accountRepository.save(fromAccount);

            if (toOpt.isPresent()) {
                Account toAccount = toOpt.get();
                toAccount.setCurrentBalance(
                toAccount.getCurrentBalance().add(BigDecimal.valueOf(txn.getAmount())));
                accountRepository.save(toAccount);

            } 
            txn.setStatus("SUCCESS");
            txn.setUtrNumber(generateUtrNumber()); 
            User user=userRepo.findByUserId(fromAccount.getUserId());
            String msg="Dear \n User Your Transaction Completed with Id "+txn.getId()+" and Ref no."+txn.getUtrNumber();
            sendUpdate(user.getEmail(),"Transaction Completed Successfully", msg);
            transactionRepo.save(txn);
        }
    }
    public void sendUpdate(String email,String subject,String msg) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(msg);
        mailSender.send(message);

    }
}
