package com.example.internetbanking.controller;
import jakarta.validation.constraints.*;
import jakarta.validation.*;
import com.example.internetbanking.entity.ScheduledPayment;
import com.example.internetbanking.service.ScheduledPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.validation.annotation.Validated;


@CrossOrigin(origins = "*")
@Validated
@RestController
@RequestMapping("/api/scheduled-payments")
public class ScheduledPaymentController {

    @Autowired
    private ScheduledPaymentService scheduledPaymentService;

    @PostMapping("/add")
    public ResponseEntity<String> scheduleBillPayment(@Valid @RequestBody ScheduledPayment payment) {

        String result = scheduledPaymentService.schedulePayment(payment);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/list")
    public ResponseEntity<List<ScheduledPayment>> getScheduledPayments(@RequestParam @Pattern(regexp = "\\d{10,20}", message = "Account number must be between 10 and 20 digits") String accountNumber) {
        List<ScheduledPayment> payments = scheduledPaymentService.getScheduledPayments(accountNumber);
        return ResponseEntity.ok(payments);
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<String> cancelScheduledPayment(
            @RequestParam 
            @NotNull(message = "Schedule ID is required") 
            @Positive(message = "Schedule ID must be a positive number")
            Long scheduleId,
            @RequestParam 
            @NotNull(message = "Account Number is required")String accountNumber) {
        
        String result = scheduledPaymentService.cancelScheduledPayment(scheduleId, accountNumber);
        return ResponseEntity.ok(result);
    }

}
