package com.hieu.ms.service;


import com.hieu.ms.entity.Chat;
import com.hieu.ms.entity.PlanType;
import com.hieu.ms.entity.Subscription;
import com.hieu.ms.entity.User;
import com.hieu.ms.repository.ChatRepository;
import com.hieu.ms.repository.SubscriptionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SubscriptionService {
    SubscriptionRepository subscriptionRepository;

    public Subscription createSubscription(User user) {
        Subscription subscription = Subscription.builder()
                .user(user)
                .subscriptionStartDate(LocalDate.now())
                .getSubscriptionEndDate(LocalDate.now())
                .isValid(true)
                .planType(PlanType.FREE)
                .build();
        return subscriptionRepository.save(subscription);
    }

    public Subscription getUsersSubscription(Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Subscription subscription = subscriptionRepository.findByUserId(user.getId());

        if (!isValid(subscription)) {
            subscription.setPlanType(PlanType.FREE);
            subscription.setGetSubscriptionEndDate(LocalDate.now().plusMonths(12));
            subscription.setSubscriptionStartDate(LocalDate.now());
        }
        return subscriptionRepository.save(subscription);
    }

    public Subscription upgradeSubscription(Authentication connectedUser, PlanType planType) {
        User user = (User) connectedUser.getPrincipal();

        Subscription subscription = subscriptionRepository.findByUserId(user.getId());
        subscription.setPlanType(planType);
        subscription.setSubscriptionStartDate(LocalDate.now());
        if (planType.equals(PlanType.ANNUALLY)) {
            subscription.setGetSubscriptionEndDate(LocalDate.now().plusMonths(12));
        } else {
            subscription.setGetSubscriptionEndDate(LocalDate.now().plusMonths(1));
        }
        return subscriptionRepository.save(subscription);
    }

    boolean isValid(Subscription subscription) {
        if (subscription.getPlanType().equals(PlanType.FREE)) {
            return true;
        }
        LocalDate endDate = subscription.getGetSubscriptionEndDate();
        LocalDate currentDate = LocalDate.now();

        return endDate.isAfter(currentDate) || endDate.isEqual(currentDate);
    }
}
