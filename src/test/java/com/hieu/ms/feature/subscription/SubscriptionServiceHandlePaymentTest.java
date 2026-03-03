package com.hieu.ms.feature.subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.hieu.ms.feature.payment.Payment;
import com.hieu.ms.feature.payment.PaymentRepository;
import com.hieu.ms.feature.payment.PaymentService;
import com.hieu.ms.feature.payment.PaymentStatus;
import com.hieu.ms.feature.user.User;
import com.hieu.ms.feature.user.UserRepository;
import com.hieu.ms.shared.event.PaymentSuccessEvent;
import com.hieu.ms.shared.exception.AppException;
import com.hieu.ms.shared.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceHandlePaymentTest {

    @Mock
    SubscriptionRepository subscriptionRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    SubscriptionAuditRepository subscriptionAuditRepository;

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    PaymentService paymentService;

    @InjectMocks
    SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(subscriptionService, "monthlyPrice", 100000);
        ReflectionTestUtils.setField(subscriptionService, "annuallyPrice", 1000000);
    }

    @Nested
    @DisplayName("handleSuccessfulPayment Tests")
    class HandleSuccessfulPaymentTests {

        @Test
        @DisplayName("Happy path: payload valid, subscription exists -> update plan MONTHLY")
        void handleSuccessfulPayment_ValidPayloadExistingSubscription_UpdatesToMonthly() {
            // Arrange
            String orderId = "ORDER-001";
            PaymentSuccessEvent event = new PaymentSuccessEvent(this, orderId);

            Payment payment = Payment.builder()
                    .externalId(orderId)
                    .payload("userId=user-1, planType=MONTHLY")
                    .status(PaymentStatus.SUCCESS)
                    .build();

            User user = User.builder().username("john").build();
            user.setId("user-1");

            Subscription subscription =
                    Subscription.builder().user(user).planType(PlanType.FREE).build();
            subscription.setId("sub-1");

            when(paymentRepository.findByExternalId(orderId)).thenReturn(Optional.of(payment));
            when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
            when(subscriptionRepository.findByUserId("user-1")).thenReturn(Optional.of(subscription));
            when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArguments()[0]);

            // Act
            subscriptionService.handleSuccessfulPayment(event);

            // Assert
            verify(subscriptionRepository).save(subscription);
            assertThat(subscription.getPlanType()).isEqualTo(PlanType.MONTHLY);
            assertThat(subscription.getSubscriptionEndDate())
                    .isEqualTo(LocalDate.now().plusMonths(1));

            verify(subscriptionAuditRepository)
                    .save(argThat(audit -> audit.getChangeType() == SubscriptionChangeType.UPDATE
                            && "MONTHLY".equals(audit.getNewPlan())
                            && "FREE".equals(audit.getOldPlan())));
        }

        @Test
        @DisplayName("Happy path: payload planType=ANNUALLY -> endDate +12 months")
        void handleSuccessfulPayment_AnnuallyPlan_SetsEndDateCorrectly() {
            // Arrange
            String orderId = "ORDER-002";
            PaymentSuccessEvent event = new PaymentSuccessEvent(this, orderId);

            Payment payment = Payment.builder()
                    .externalId(orderId)
                    .payload("userId=user-1, planType=ANNUALLY")
                    .status(PaymentStatus.SUCCESS)
                    .build();

            User user = User.builder().username("john").build();
            user.setId("user-1");

            Subscription subscription =
                    Subscription.builder().user(user).planType(PlanType.FREE).build();

            when(paymentRepository.findByExternalId(orderId)).thenReturn(Optional.of(payment));
            when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
            when(subscriptionRepository.findByUserId("user-1")).thenReturn(Optional.of(subscription));
            when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArguments()[0]);

            // Act
            subscriptionService.handleSuccessfulPayment(event);

            // Assert
            assertThat(subscription.getPlanType()).isEqualTo(PlanType.ANNUALLY);
            assertThat(subscription.getSubscriptionEndDate())
                    .isEqualTo(LocalDate.now().plusMonths(12));

            verify(subscriptionAuditRepository).save(argThat(audit -> "ANNUALLY".equals(audit.getNewPlan())));
        }

        @Test
        @DisplayName("Subscription null -> create new then update")
        void handleSuccessfulPayment_SubscriptionMissing_CreatesNew() {
            // Arrange
            String orderId = "ORDER-003";
            PaymentSuccessEvent event = new PaymentSuccessEvent(this, orderId);

            Payment payment = Payment.builder()
                    .externalId(orderId)
                    .payload("userId=user-1, planType=MONTHLY")
                    .status(PaymentStatus.SUCCESS)
                    .build();

            User user = User.builder().username("john").build();
            user.setId("user-1");

            when(paymentRepository.findByExternalId(orderId)).thenReturn(Optional.of(payment));
            when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
            when(subscriptionRepository.findByUserId("user-1")).thenReturn(Optional.empty());
            when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArguments()[0]);

            // Act
            subscriptionService.handleSuccessfulPayment(event);

            // Assert
            verify(subscriptionRepository).save(any(Subscription.class));
            verify(subscriptionAuditRepository)
                    .save(argThat(audit -> "FREE".equals(audit.getOldPlan()) && "MONTHLY".equals(audit.getNewPlan())));
        }

        @Test
        @DisplayName("payload null -> throws INVALID_REQUEST")
        void handleSuccessfulPayment_NullPayload_ThrowsInvalidRequest() {
            // Arrange
            String orderId = "ORDER-004";
            PaymentSuccessEvent event = new PaymentSuccessEvent(this, orderId);

            Payment payment = Payment.builder()
                    .externalId(orderId)
                    .payload(null)
                    .status(PaymentStatus.SUCCESS)
                    .build();

            when(paymentRepository.findByExternalId(orderId)).thenReturn(Optional.of(payment));

            // Act & Assert
            assertThatThrownBy(() -> subscriptionService.handleSuccessfulPayment(event))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);

            verify(subscriptionRepository, never()).save(any());
        }

        @Test
        @DisplayName("payload missing userId -> throws INVALID_REQUEST")
        void handleSuccessfulPayment_MissingUserIdInPayload_ThrowsInvalidRequest() {
            // Arrange
            String orderId = "ORDER-005";
            PaymentSuccessEvent event = new PaymentSuccessEvent(this, orderId);

            Payment payment = Payment.builder()
                    .externalId(orderId)
                    .payload("planType=MONTHLY")
                    .status(PaymentStatus.SUCCESS)
                    .build();

            when(paymentRepository.findByExternalId(orderId)).thenReturn(Optional.of(payment));

            // Act & Assert
            assertThatThrownBy(() -> subscriptionService.handleSuccessfulPayment(event))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);
        }

        @Test
        @DisplayName("Payment not found -> throws PAYMENT_NOT_FOUND")
        void handleSuccessfulPayment_PaymentNotFound_ThrowsPaymentNotFound() {
            // Arrange
            String orderId = "ORDER-404";
            PaymentSuccessEvent event = new PaymentSuccessEvent(this, orderId);

            when(paymentRepository.findByExternalId(orderId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> subscriptionService.handleSuccessfulPayment(event))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_NOT_FOUND);

            verify(userRepository, never()).findById(any());
        }

        @Test
        @DisplayName("User not found -> throws USER_NOT_EXISTED")
        void handleSuccessfulPayment_UserNotFound_ThrowsUserNotExisted() {
            // Arrange
            String orderId = "ORDER-006";
            PaymentSuccessEvent event = new PaymentSuccessEvent(this, orderId);

            Payment payment = Payment.builder()
                    .externalId(orderId)
                    .payload("userId=ghost-id, planType=MONTHLY")
                    .status(PaymentStatus.SUCCESS)
                    .build();

            when(paymentRepository.findByExternalId(orderId)).thenReturn(Optional.of(payment));
            when(userRepository.findById("ghost-id")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> subscriptionService.handleSuccessfulPayment(event))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_EXISTED);

            verify(subscriptionRepository, never()).save(any());
        }

        @Test
        @DisplayName("payload planType null/invalid -> fallback MONTHLY")
        void handleSuccessfulPayment_InvalidPlanType_FallsBackToMonthly() {
            // Arrange
            String orderId = "ORDER-007";
            PaymentSuccessEvent event = new PaymentSuccessEvent(this, orderId);

            Payment payment = Payment.builder()
                    .externalId(orderId)
                    .payload("userId=user-1, planType=INVALID")
                    .status(PaymentStatus.SUCCESS)
                    .build();

            User user = User.builder().username("john").build();
            user.setId("user-1");

            Subscription subscription =
                    Subscription.builder().user(user).planType(PlanType.FREE).build();

            when(paymentRepository.findByExternalId(orderId)).thenReturn(Optional.of(payment));
            when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
            when(subscriptionRepository.findByUserId("user-1")).thenReturn(Optional.of(subscription));
            when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArguments()[0]);

            // Act
            subscriptionService.handleSuccessfulPayment(event);

            // Assert
            assertThat(subscription.getPlanType()).isEqualTo(PlanType.MONTHLY);
            verify(subscriptionAuditRepository).save(argThat(audit -> "MONTHLY".equals(audit.getNewPlan())));
        }
    }
}
