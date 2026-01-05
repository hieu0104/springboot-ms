package com.hieu.ms.feature.payment;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByExternalId(String externalId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.externalId = :externalId")
    Optional<Payment> findByExternalIdWithLock(@Param("externalId") String externalId);

    boolean existsByExternalIdAndStatus(String externalId, String status);

    boolean existsByExternalIdAndStatus(String externalId, PaymentStatus status);
}
