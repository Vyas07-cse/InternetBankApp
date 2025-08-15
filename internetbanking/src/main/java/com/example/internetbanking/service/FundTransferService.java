package com.example.internetbanking.service;

import com.example.internetbanking.dto.FundTransferRequest;
import com.example.internetbanking.dto.FundTransferResponse;
import com.example.internetbanking.entity.Beneficiary;
import com.example.internetbanking.exception.BeneficiaryFoundException;

public interface FundTransferService {

    FundTransferResponse transfer(FundTransferRequest request);

    String addBeneficiary(Beneficiary beneficiary) throws BeneficiaryFoundException;

    String verifyOtp(String beneficiaryId, String inputOtp);

    void sendOtp(String toEmail, String otp);

    String getTxnStatus(String id);
}
