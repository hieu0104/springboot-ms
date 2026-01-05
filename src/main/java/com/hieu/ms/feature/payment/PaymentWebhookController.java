package com.hieu.ms.feature.payment;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class PaymentWebhookController {
    private final PaymentService paymentService;

    @PostMapping("/vnpay")
    public ResponseEntity<String> handleVnpay(HttpServletRequest request) {
        // Delegate to PaymentService which calls VNPayService for verification
        boolean processed = paymentService.processCallback("VNPAY", request);
        return ResponseEntity.ok(processed ? "OK" : "IGNORED");
    }

    @PostMapping("/momo")
    public ResponseEntity<String> handleMomo(@org.springframework.web.bind.annotation.RequestBody Object callbackData) {
        // Delegate to PaymentService which calls MomoPaymentProvider for verification
        boolean processed = paymentService.processCallback("MOMO", callbackData);
        return ResponseEntity.ok(processed ? "OK" : "IGNORED");
    }
}
