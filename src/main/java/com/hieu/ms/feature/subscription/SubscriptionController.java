package com.hieu.ms.feature.subscription;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.hieu.ms.feature.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscription Management", description = "APIs quản lý gói dịch vụ")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;

    @GetMapping("/user")
    @Operation(
            summary = "Lấy gói dịch vụ của người dùng",
            description = "Lấy thông tin gói dịch vụ hiện tại của người dùng")
    public ResponseEntity<Subscription> getUsersSubscription(Authentication connectedUser) {
        Subscription subscription = subscriptionService.getUsersSubscription(connectedUser);
        return new ResponseEntity<>(subscription, HttpStatus.OK);
    }

    @PostMapping("/upgrade")
    @Operation(
            summary = "Nâng cấp gói dịch vụ",
            description = "Tạo link thanh toán để nâng cấp gói dịch vụ (Hỗ trợ VNPAY, MOMO)")
    public ResponseEntity<String> upgradeSubscription(
            Authentication connectedUser,
            @RequestParam PlanType planType,
            @RequestParam(required = false) String provider) {

        // Gọi service để tạo link thanh toán
        String paymentUrl = subscriptionService.generatePaymentUrl(connectedUser, planType, provider);
        return new ResponseEntity<>(paymentUrl, HttpStatus.OK);
    }
}
