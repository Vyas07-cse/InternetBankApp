package com.example.internetbanking.controller;
import jakarta.validation.constraints.*;
import jakarta.validation.*;
import org.springframework.validation.annotation.Validated;
import com.example.internetbanking.dto.PayBillRequest;
import com.example.internetbanking.service.impl.BillPaymentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.example.internetbanking.exception.BillPaymentException;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.example.internetbanking.entity.BillPayment;

@Validated
@RestController
@RequestMapping("/api/bills")
public class BillPaymentController {
    @Autowired
    private BillPaymentServiceImpl billPaymentService;

    @PostMapping("/pay")
    public ResponseEntity<String> payBill(@RequestBody @Valid PayBillRequest request) {
    	
        String result = billPaymentService.payBill(request.getAccountNumber(), 
        		                                    request.getBillType(), 
        		                                    request.getBillAmount(), 
        		                                    request.getServiceNumber());
        return ResponseEntity.ok(result);
    }
    @GetMapping("/history")
    public ResponseEntity<List<BillPayment>> getBillHistory(@RequestParam @Pattern(regexp = "\\d{10,20}") String accountNumber) {
        List<BillPayment> payments = billPaymentService.getBillPaymentHistory(accountNumber);
        if (payments.isEmpty()) {
            throw new BillPaymentException("No payments found");
        }
        return ResponseEntity.ok(payments); 
    }

}
