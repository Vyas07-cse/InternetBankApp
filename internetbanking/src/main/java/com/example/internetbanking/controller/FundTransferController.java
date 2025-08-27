package com.example.internetbanking.controller;

import com.example.internetbanking.dto.FundTransferRequest;
import com.example.internetbanking.dto.FundTransferResponse;
import com.example.internetbanking.dto.OtpVerifyRequest;
import com.example.internetbanking.entity.Beneficiary;
import com.example.internetbanking.exception.BeneficiaryFoundException;
import com.example.internetbanking.repository.BeneficiaryRepository;
import com.example.internetbanking.service.FundTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.internetbanking.repository.UserRepository;
import com.example.internetbanking.exception.UserNotFoundException;
import jakarta.validation.*;
import org.springframework.validation.annotation.Validated;
import java.util.List;
@Validated
@RestController
@RequestMapping("/api/transfer")
public class FundTransferController {

    @Autowired 
    private FundTransferService transferService;

    @Autowired 
    private BeneficiaryRepository beneficiaryRepo;
    
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<FundTransferResponse> transfer(@Valid @RequestBody FundTransferRequest request) {
        FundTransferResponse response = transferService.transfer(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/beneficiaries/user/{userId}")
    public List<Beneficiary> getBeneficiaries(@PathVariable String userId) {
        return beneficiaryRepo.findByUserId(userId);
    }

    @PostMapping("/beneficiaries/add")
    public ResponseEntity<String> addBeneficiary(@Valid @RequestBody Beneficiary beneficiary) throws BeneficiaryFoundException {
    	if (!userRepository.existsById(beneficiary.getUserId())) {
    	    throw new UserNotFoundException("User with ID " + beneficiary.getUserId() + " not found.");
    	}
        String savedBeneficiary = transferService.addBeneficiary(beneficiary);
        return ResponseEntity.ok(savedBeneficiary);
    }
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(transferService.verifyOtp(request.getBeneficiaryId(), request.getOtp()));
    }
    
    @GetMapping("/status/{id}")
    public ResponseEntity<String> getTxnStatus(@PathVariable String id){
    	return ResponseEntity.ok(transferService.getTxnStatus(id));
    }
    
    

}
