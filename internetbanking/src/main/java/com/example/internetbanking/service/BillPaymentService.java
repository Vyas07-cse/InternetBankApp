package com.example.internetbanking.service;
import com.example.internetbanking.entity.BillPayment;
import java.math.*;
import java.util.*;
public interface BillPaymentService {
    String payBill(String accountNumber, String billType, BigDecimal billAmount,String serviceNumber);
    List<BillPayment> getBillPaymentHistory(String accountNumber);

}
