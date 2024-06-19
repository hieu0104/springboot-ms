//package com.hieu.ms.service;
//
//
//import com.hieu.ms.entity.Chat;
//import com.hieu.ms.entity.PlanType;
//import com.hieu.ms.repository.ChatRepository;
//import com.paypal.api.payments.*;
//import com.paypal.base.rest.APIContext;
//import com.paypal.base.rest.PayPalRESTException;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cglib.core.Local;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Service;
//import org.springframework.web.bind.annotation.PathVariable;
//
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//import java.util.ResourceBundle;
//
//@Service
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//@Slf4j
//public class PaymentService {
//    APIContext apiContext;
////    public ResponseEntity<?> createPayment(
////            @PathVariable PlanType planType,
////            Authentication connectedUser
////    ) {
////        int amount = 799 * 100;
////        if (planType.equals(PlanType.ANNUALLY)) {
////            amount = amount * 12;
////            amount = (int) (amount * 0.7);
////        }
////        return null;
////    }
//
//    public Payment createPayment(
//            Double total,
//            String currency,
//            String method,
//            String intent,
//            String description,
//            String cancelUrl,
//            String successUrl
//    ) throws PayPalRESTException {
//        Amount amount = new Amount();
//        amount.setCurrency(currency);
//        amount.setTotal(
//                String.format(Locale.forLanguageTag(currency),
//                        "%.2f", total)
//        );
//        Transaction transaction = new Transaction();
//        transaction.setDescription(description);
//        transaction.setAmount(amount);
//        List<Transaction> transactions = new ArrayList<>();
//        transactions.add(transaction);
//
//        Payer payer = new Payer();
//        payer.setPaymentMethod(method);
//
//        Payment payment = new Payment();
//        payment.setIntent(intent);
//        payment.setPayer(payer);
//        payment.setTransactions(transactions);
//
//        RedirectUrls redirectUrls = new RedirectUrls();
//        redirectUrls.setCancelUrl(cancelUrl);
//        redirectUrls.setReturnUrl(successUrl);
//
//        payment.setRedirectUrls(redirectUrls);
//        return payment.create(apiContext);
//    }
//
//
//}
