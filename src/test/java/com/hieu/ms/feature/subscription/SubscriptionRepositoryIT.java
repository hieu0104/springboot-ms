package com.hieu.ms.feature.subscription;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration Test — SubscriptionRepository
 *
 * Test loại: INTEGRATION TEST (real MySQL via Testcontainers)
 * Verifies: persist/read đúng, @Version tăng sau update (optimistic locking thật)
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class SubscriptionRepositoryIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
    }

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Test
    @DisplayName("save: should persist Subscription and assign UUID id")
    void save_shouldPersistAndAssignId() {
        // Arrange
        Subscription sub = Subscription.builder()
                .subscriptionStartDate(LocalDate.now())
                .subscriptionEndDate(LocalDate.now().plusYears(100))
                .planType(PlanType.FREE)
                .isValid(true)
                .build();

        // Act
        Subscription saved = subscriptionRepository.saveAndFlush(sub);

        // Assert
        assertThat(saved.getId()).isNotNull().isNotBlank();
        assertThat(saved.getPlanType()).isEqualTo(PlanType.FREE);
        assertThat(saved.isValid()).isTrue();
        assertThat(saved.getSubscriptionStartDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("findById: should reload persisted Subscription from DB")
    void findById_shouldReturnSavedSubscription() {
        // Arrange
        Subscription sub = Subscription.builder()
                .subscriptionStartDate(LocalDate.now())
                .subscriptionEndDate(LocalDate.now().plusYears(100))
                .planType(PlanType.FREE)
                .isValid(true)
                .build();
        Subscription saved = subscriptionRepository.saveAndFlush(sub);

        // Act
        Subscription loaded = subscriptionRepository.findById(saved.getId()).orElseThrow();

        // Assert
        assertThat(loaded.getId()).isEqualTo(saved.getId());
        assertThat(loaded.getPlanType()).isEqualTo(PlanType.FREE);
    }

    @Test
    @DisplayName("findById: non-existent id should return empty")
    void findById_nonExistent_shouldReturnEmpty() {
        assertThat(subscriptionRepository.findById("non-existent-id")).isEmpty();
    }

    @Test
    @DisplayName("@Version: version increments after each update — optimistic locking works")
    void version_shouldIncrementAfterUpdate() {
        // Arrange: initial save
        Subscription sub = Subscription.builder()
                .subscriptionStartDate(LocalDate.now())
                .subscriptionEndDate(LocalDate.now().plusYears(100))
                .planType(PlanType.FREE)
                .isValid(true)
                .build();
        Subscription saved = subscriptionRepository.saveAndFlush(sub);
        Long versionAfterCreate = saved.getVersion();

        // Act: update plan type and save again
        saved.setPlanType(PlanType.MONTHLY);
        saved.setSubscriptionEndDate(LocalDate.now().plusMonths(1));
        Subscription updated = subscriptionRepository.saveAndFlush(saved);

        // Assert
        assertThat(updated.getVersion()).isNotNull().isGreaterThan(versionAfterCreate);
    }
}
