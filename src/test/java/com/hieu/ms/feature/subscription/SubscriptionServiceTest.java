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
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import com.hieu.ms.feature.payment.PaymentRepository;
import com.hieu.ms.feature.payment.PaymentService;
import com.hieu.ms.feature.user.User;
import com.hieu.ms.feature.user.UserRepository;
import com.hieu.ms.shared.exception.AppException;
import com.hieu.ms.shared.exception.ErrorCode;

/**
 * Unit Test — SubscriptionService
 *
 * Test loại: UNIT TEST (mock tất cả dependencies)
 * Framework: JUnit 5 + Mockito + AssertJ
 * Pattern: AAA (Arrange - Act - Assert)
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

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

    @Mock
    Authentication authentication;

    @InjectMocks
    SubscriptionService subscriptionService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Inject @Value fields that Spring doesn't set in pure unit tests
        ReflectionTestUtils.setField(subscriptionService, "monthlyPrice", 100000);
        ReflectionTestUtils.setField(subscriptionService, "annuallyPrice", 1000000);

        testUser = User.builder().username("testuser").build();
        testUser.setId("user-1");
    }

    // ─── isValid ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("isValid")
    class IsValidTests {

        @Test
        @DisplayName("null subscription → false")
        void null_returnsFalse() {
            assertThat(subscriptionService.isValid(null)).isFalse();
        }

        @Test
        @DisplayName("null planType → false")
        void nullPlanType_returnsFalse() {
            Subscription sub = Subscription.builder().build(); // planType = null
            assertThat(subscriptionService.isValid(sub)).isFalse();
        }

        @Test
        @DisplayName("FREE plan → always true regardless of dates")
        void freePlan_alwaysTrue() {
            Subscription sub = Subscription.builder()
                    .planType(PlanType.FREE)
                    .subscriptionEndDate(LocalDate.now().minusYears(1)) // even a past date
                    .build();

            assertThat(subscriptionService.isValid(sub)).isTrue();
        }

        @Test
        @DisplayName("MONTHLY with past endDate → false (expired)")
        void monthlyExpired_returnsFalse() {
            Subscription sub = Subscription.builder()
                    .planType(PlanType.MONTHLY)
                    .subscriptionEndDate(LocalDate.now().minusDays(1))
                    .build();

            assertThat(subscriptionService.isValid(sub)).isFalse();
        }

        @Test
        @DisplayName("MONTHLY with endDate = today → true (still valid)")
        void monthlyEndsToday_returnsTrue() {
            Subscription sub = Subscription.builder()
                    .planType(PlanType.MONTHLY)
                    .subscriptionEndDate(LocalDate.now())
                    .build();

            assertThat(subscriptionService.isValid(sub)).isTrue();
        }

        @Test
        @DisplayName("MONTHLY with future endDate → true")
        void monthlyFuture_returnsTrue() {
            Subscription sub = Subscription.builder()
                    .planType(PlanType.MONTHLY)
                    .subscriptionEndDate(LocalDate.now().plusDays(10))
                    .build();

            assertThat(subscriptionService.isValid(sub)).isTrue();
        }

        @Test
        @DisplayName("null endDate on paid plan → false")
        void nullEndDate_returnsFalse() {
            Subscription sub = Subscription.builder()
                    .planType(PlanType.ANNUALLY)
                    .subscriptionEndDate(null)
                    .build();

            assertThat(subscriptionService.isValid(sub)).isFalse();
        }
    }

    // ─── createSubscription ───────────────────────────────────────────────────

    @Nested
    @DisplayName("createSubscription")
    class CreateSubscriptionTests {

        @Test
        @DisplayName("should save FREE plan, write CREATE audit, and return saved subscription")
        void shouldSaveFREEAndAudit() {
            // Arrange
            Subscription saved = Subscription.builder()
                    .planType(PlanType.FREE)
                    .isValid(true)
                    .subscriptionStartDate(LocalDate.now())
                    .subscriptionEndDate(LocalDate.now().plusYears(100))
                    .build();
            saved.setId("sub-1");

            when(subscriptionRepository.save(any(Subscription.class))).thenReturn(saved);
            when(subscriptionAuditRepository.save(any(SubscriptionAudit.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Subscription result = subscriptionService.createSubscription(testUser);

            // Assert
            assertThat(result.getPlanType()).isEqualTo(PlanType.FREE);
            assertThat(result.isValid()).isTrue();
            verify(subscriptionRepository).save(any(Subscription.class));
            verify(subscriptionAuditRepository)
                    .save(argThat(audit -> audit.getChangeType() == SubscriptionChangeType.CREATE
                            && "FREE".equals(audit.getNewPlan())
                            && audit.getOldPlan() == null));
        }
    }

    // ─── getUsersSubscription ─────────────────────────────────────────────────

    @Nested
    @DisplayName("getUsersSubscription")
    class GetUsersSubscriptionTests {

        @Test
        @DisplayName("user not found → throws USER_NOT_EXISTED")
        void userNotFound_shouldThrow() {
            when(authentication.getName()).thenReturn("unknown");
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionService.getUsersSubscription(authentication))
                    .isInstanceOf(AppException.class)
                    .satisfies(
                            ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.USER_NOT_EXISTED));
        }

        @Test
        @DisplayName("no existing subscription → auto-creates FREE subscription")
        void noSubscription_shouldAutoCreate() {
            Subscription created =
                    Subscription.builder().planType(PlanType.FREE).isValid(true).build();
            created.setId("sub-new");

            when(authentication.getName()).thenReturn("testuser");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(subscriptionRepository.findByUserId("user-1")).thenReturn(Optional.empty());
            when(subscriptionRepository.save(any(Subscription.class))).thenReturn(created);
            when(subscriptionAuditRepository.save(any(SubscriptionAudit.class))).thenAnswer(inv -> inv.getArgument(0));

            Subscription result = subscriptionService.getUsersSubscription(authentication);

            assertThat(result.getPlanType()).isEqualTo(PlanType.FREE);
            verify(subscriptionRepository).save(any(Subscription.class));
        }

        @Test
        @DisplayName("valid subscription → returns it as-is")
        void validSubscription_shouldReturn() {
            Subscription valid = Subscription.builder()
                    .planType(PlanType.MONTHLY)
                    .subscriptionEndDate(LocalDate.now().plusDays(15))
                    .isValid(true)
                    .build();

            when(authentication.getName()).thenReturn("testuser");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(subscriptionRepository.findByUserId("user-1")).thenReturn(Optional.of(valid));

            Subscription result = subscriptionService.getUsersSubscription(authentication);

            assertThat(result).isEqualTo(valid);
            verify(subscriptionRepository, never()).save(any()); // no save needed
        }

        @Test
        @DisplayName("expired subscription → downgrades to FREE and writes UPDATE audit")
        void expiredSubscription_shouldDowngradeToFREE() {
            Subscription expired = Subscription.builder()
                    .planType(PlanType.MONTHLY)
                    .subscriptionEndDate(LocalDate.now().minusDays(1))
                    .isValid(true)
                    .build();
            expired.setId("sub-expired");

            Subscription downgraded =
                    Subscription.builder().planType(PlanType.FREE).isValid(true).build();

            when(authentication.getName()).thenReturn("testuser");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(subscriptionRepository.findByUserId("user-1")).thenReturn(Optional.of(expired));
            when(subscriptionRepository.save(expired)).thenReturn(downgraded);
            when(subscriptionAuditRepository.save(any(SubscriptionAudit.class))).thenAnswer(inv -> inv.getArgument(0));

            Subscription result = subscriptionService.getUsersSubscription(authentication);

            assertThat(result.getPlanType()).isEqualTo(PlanType.FREE);
            verify(subscriptionRepository).save(expired);
            verify(subscriptionAuditRepository)
                    .save(argThat(audit -> audit.getChangeType() == SubscriptionChangeType.UPDATE
                            && "MONTHLY".equals(audit.getOldPlan())
                            && "FREE".equals(audit.getNewPlan())));
        }
    }

    // ─── upgradeSubscription ──────────────────────────────────────────────────

    @Nested
    @DisplayName("upgradeSubscription")
    class UpgradeSubscriptionTests {

        @Test
        @DisplayName("user not found → throws USER_NOT_EXISTED")
        void userNotFound_shouldThrow() {
            when(authentication.getName()).thenReturn("ghost");
            when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionService.upgradeSubscription(authentication, PlanType.MONTHLY))
                    .isInstanceOf(AppException.class)
                    .satisfies(
                            ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.USER_NOT_EXISTED));
        }

        @Test
        @DisplayName("existing FREE subscription → upgrades plan and saves audit")
        void existingFree_shouldUpgradeToPaid() {
            Subscription freeSub = Subscription.builder()
                    .planType(PlanType.FREE)
                    .isValid(true)
                    .subscriptionEndDate(LocalDate.now().plusYears(100))
                    .build();
            freeSub.setId("sub-1");

            Subscription upgraded = Subscription.builder()
                    .planType(PlanType.MONTHLY)
                    .isValid(true)
                    .build();

            when(authentication.getName()).thenReturn("testuser");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(subscriptionRepository.findByUserId("user-1")).thenReturn(Optional.of(freeSub));
            when(subscriptionRepository.save(freeSub)).thenReturn(upgraded);
            when(subscriptionAuditRepository.save(any(SubscriptionAudit.class))).thenAnswer(inv -> inv.getArgument(0));

            Subscription result = subscriptionService.upgradeSubscription(authentication, PlanType.MONTHLY);

            assertThat(result.getPlanType()).isEqualTo(PlanType.MONTHLY);
            verify(subscriptionRepository).save(freeSub);
            verify(subscriptionAuditRepository)
                    .save(argThat(audit -> audit.getChangeType() == SubscriptionChangeType.UPDATE
                            && "FREE".equals(audit.getOldPlan())
                            && "MONTHLY".equals(audit.getNewPlan())));
        }

        @Test
        @DisplayName("no existing subscription → auto-creates then upgrades")
        void noSubscription_shouldCreateThenUpgrade() {
            Subscription autoCreated =
                    Subscription.builder().planType(PlanType.FREE).isValid(true).build();
            autoCreated.setId("sub-new");

            Subscription upgraded = Subscription.builder()
                    .planType(PlanType.ANNUALLY)
                    .isValid(true)
                    .build();

            when(authentication.getName()).thenReturn("testuser");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(subscriptionRepository.findByUserId("user-1")).thenReturn(Optional.empty());
            // First save: createSubscription; second save: upgradeSubscription
            when(subscriptionRepository.save(any(Subscription.class)))
                    .thenReturn(autoCreated)
                    .thenReturn(upgraded);
            when(subscriptionAuditRepository.save(any(SubscriptionAudit.class))).thenAnswer(inv -> inv.getArgument(0));

            Subscription result = subscriptionService.upgradeSubscription(authentication, PlanType.ANNUALLY);

            assertThat(result.getPlanType()).isEqualTo(PlanType.ANNUALLY);
            // save called twice: once for createSubscription, once for upgradeSubscription
            verify(subscriptionRepository, times(2)).save(any(Subscription.class));
        }
    }
}
