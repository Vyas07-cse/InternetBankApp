package com.example.internetbanking.service.impl;

import com.example.internetbanking.dto.FundTransferRequest;
import com.example.internetbanking.dto.FundTransferResponse;
import com.example.internetbanking.entity.Account;
import com.example.internetbanking.entity.Transaction;
import com.example.internetbanking.entity.User;
import com.example.internetbanking.entity.Beneficiary;
import com.example.internetbanking.repository.AccountRepository;
import com.example.internetbanking.repository.TransactionRepository;
import com.example.internetbanking.repository.UserRepository;
import com.example.internetbanking.repository.BeneficiaryRepository;
import com.example.internetbanking.exception.BeneficiaryFoundException;
import com.example.internetbanking.exception.CustomException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.internetbanking.service.FundTransferService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

@Service

public class FundTransferServiceImpl implements FundTransferService{
	 @Autowired
	 private JavaMailSender mailSender;

    @Autowired
    private AccountRepository accountRepo;
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private TransactionRepository transactionRepo;

    @Autowired
    private BeneficiaryRepository beneficiaryRepo;
    
    private static final Logger log = LoggerFactory.getLogger(FundTransferServiceImpl.class);


    public FundTransferResponse transfer(FundTransferRequest request) {
    	
        String transferType = request.getTransferType().toUpperCase();
        if (!transferType.equals("IMPS") && !transferType.equals("NEFT")) {
            throw new CustomException("Invalid transfer type. Allowed: IMPS, NEFT");
        }

        Optional<Account> fromAccOpt = accountRepo.findByAccountNumber(request.getFromAccount());
        if (fromAccOpt.isEmpty()) {
            throw new CustomException("Sender account not found");
        }
        Account fromAccount = fromAccOpt.get();
        if (fromAccount.getTpin() != request.getTpin()) {
            throw new CustomException("Invalid TPIN");
        }

        LocalDateTime now = LocalDateTime.now();
        if (request.getScheduledDate() == null || !request.getScheduledDate().isAfter(now)) {
        	if (fromAccount.getCurrentBalance()<request.getAmount()) {
                throw new CustomException("Insufficient balance");
            }
        }

        boolean isIntraBank = accountRepo.findByAccountNumber(request.getToAccount()).isPresent();

        if (request.getScheduledDate() != null ) {
        	if (!request.getScheduledDate().isAfter(now)) {
                throw new CustomException("Scheduled date must be in the future.");
            }
             saveScheduledTransfer(request, "PENDING");
             return new FundTransferResponse("scheduled", "Transfer scheduled successfully",null);
        } else {
                if (isIntraBank) {
                	log.info("called");
                return handleIntraBankTransfer(fromAccount, request.getToAccount(), request.getAmount(), transferType);
            } else {
                return handleInterBankTransfer(fromAccount, request.getToAccount(), request.getAmount(), transferType);
            }
        }
    }

    private void saveScheduledTransfer(FundTransferRequest request, String status) {
        Transaction txn = new Transaction();
        txn.setId(getTxnId());
        txn.setFromAccount(request.getFromAccount());
        txn.setToAccount(request.getToAccount());
        txn.setAmount(request.getAmount());
        txn.setMode(request.getTransferType());
        txn.setStatus(status);
        txn.setDateTime(LocalDateTime.now());
        txn.setScheduledDate(request.getScheduledDate());
        txn.setUtrNumber(null);
        Optional<Account> acountlist=accountRepo.findByAccountNumber(request.getFromAccount());
        Account account=acountlist.get();
        User user=userRepo.findByUserId(account.getUserId());
        String msg="Dear, \n User Your Transaction Scheduled with Id "+txn.getId();
        sendUpdate(user.getEmail(),"Transaction Scheduled",msg);
        transactionRepo.save(txn);
    }

  

    private FundTransferResponse handleIntraBankTransfer(Account fromAccount, String toAccountNumber, double amount, String transferType) {
        Account toAccount = accountRepo.findByAccountNumber(toAccountNumber).get();

        fromAccount.setCurrentBalance(fromAccount.getCurrentBalance()-amount);
        toAccount.setCurrentBalance(toAccount.getCurrentBalance()+amount);

        accountRepo.save(fromAccount);
        accountRepo.save(toAccount);
         
        String utr = generateUtrNumber();
        saveTransaction(fromAccount.getAccountNumber(), toAccount.getAccountNumber(), amount, "INTRA-" + transferType, "SUCCESS", LocalDateTime.now(), utr);

        return new FundTransferResponse("completed","success",utr);
    }

    private FundTransferResponse handleInterBankTransfer(Account fromAccount, String toAccountNumber, double amount, String transferType) {

        fromAccount.setCurrentBalance(fromAccount.getCurrentBalance()-amount);
        accountRepo.save(fromAccount);
       
        String utr = generateUtrNumber();
        saveTransaction(
            fromAccount.getAccountNumber(),
            toAccountNumber,
            amount,
            "INTER-" + transferType,
            "PENDING",
            LocalDateTime.now(),
            utr
        );

        return new FundTransferResponse("completed", "success",utr);
    }


    private void saveTransaction(String fromAccount, String toAccount, double amount, String mode, String status, LocalDateTime dateTime,String utrNumber) {
        Transaction txn = new Transaction();
        Optional<Account> acountlist=accountRepo.findByAccountNumber(fromAccount);
        Account account=acountlist.get();
        User user=userRepo.findByUserId(account.getUserId());
        txn.setId(getTxnId());
        txn.setFromAccount(fromAccount);
        txn.setToAccount(toAccount);
        txn.setAmount(amount);
        txn.setMode(mode);
        txn.setStatus(status);
        txn.setDateTime(dateTime);
        txn.setUtrNumber(utrNumber); 
        String msg="Dear, \n User Your Transaction Completed with Id "+txn.getId()+"and Ref no."+txn.getUtrNumber();
        sendUpdate(user.getEmail(),"Transaction Successfully",msg);
        
        transactionRepo.save(txn);
    }
    private String getTxnId() {
    	 int id = (int)(Math.random() * 9000) + 1000; 
    	return "TXN"+id;
    }


    public String addBeneficiary(Beneficiary beneficiary) throws BeneficiaryFoundException {
    	if(beneficiaryRepo.existsByAccountNumberAndUserId(beneficiary.getAccountNumber(),beneficiary.getUserId())) {
    		throw new BeneficiaryFoundException("Beneficiary Already Exists");
    	}
        beneficiary.setBeneficiaryId(generateBeneficiaryId());
        beneficiary.setVerified(false);
        beneficiary.setCreatedAt(LocalDateTime.now());
        String otp = generateOtp();
        beneficiary.setOtp(otp);
        beneficiaryRepo.save(beneficiary);
        User user=userRepo.findByUserId(beneficiary.getUserId());
        sendUpdate(user.getEmail(),"Beneficiary Verify","Dear User,\n\nYour OTP for beneficiary verification is: " + otp +" for ID "+beneficiary.getBeneficiaryId()+
                "\n\nPlease use this OTP to verify the beneficiary within the next 10 minutes." +
                "\n\nThank you,\nYour Bank Team");
        return "OTP sent for verification for Id "+beneficiary.getBeneficiaryId();
    }

   
    private String generateOtp() {
        int otp = (int)(Math.random() * 9000) + 1000; 
        return String.valueOf(otp);
    }

   
    public String verifyOtp(String beneficiaryId, String inputOtp) {
        Beneficiary ben = beneficiaryRepo.findById(beneficiaryId)
            .orElseThrow(() -> new CustomException("Beneficiary not found"));

        if (ben.getOtp() != null && ben.getOtp().equals(inputOtp)) {
            ben.setVerified(true);
            ben.setOtp(null);
            beneficiaryRepo.save(ben);
            return "Beneficiary verified successfully!";
        } else {
            throw new CustomException("Invalid OTP");
        }
    }

    public void sendOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("OTP Verification - Beneficiary Registration");
        message.setText("Dear User,\n\nYour OTP for beneficiary verification is: " + otp +
                        "\n\nPlease use this OTP to verify the beneficiary within the next 10 minutes." +
                        "\n\nThank you,\nYour Bank Team");

        mailSender.send(message);
    }
    public void sendUpdate(String email,String subject,String msg) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(msg);
        mailSender.send(message);

    }
    private String generateBeneficiaryId() {
        long count = beneficiaryRepo.count();  
        long nextNumber = 1000 + count + 1;
        return "bny" + nextNumber;
    }
    
    private String generateUtrNumber() {
        return "UTR" + System.currentTimeMillis(); 
    }
    public String getTxnStatus(String id) {
    	return transactionRepo.findById(id).get().getStatus();
    }




}
