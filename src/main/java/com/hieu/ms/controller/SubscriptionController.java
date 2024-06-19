package com.hieu.ms.controller;

import com.hieu.ms.entity.PlanType;
import com.hieu.ms.entity.Subscription;
import com.hieu.ms.service.SubscriptionService;
import com.hieu.ms.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;



    @GetMapping("/user")
    public ResponseEntity<Subscription> getUsersSubscription(Authentication connectedUser) {
        Subscription subscription = subscriptionService.getUsersSubscription(connectedUser);
        return new ResponseEntity<>(subscription, HttpStatus.OK);
    }

    @PostMapping("/upgrade")
    public ResponseEntity<Subscription> upgradeSubscription(Authentication connectedUser,
                                                            @RequestParam PlanType planType) {
        Subscription subscription = subscriptionService.upgradeSubscription(connectedUser, planType);
        return new ResponseEntity<>(subscription, HttpStatus.OK);
    }
}
