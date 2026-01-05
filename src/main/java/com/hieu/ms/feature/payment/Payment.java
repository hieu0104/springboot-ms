package com.hieu.ms.feature.payment;

import java.time.Instant;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        indexes = {
            @Index(name = "idx_payment_external_id", columnList = "externalId"),
            @Index(name = "idx_payment_status", columnList = "status"),
            @Index(name = "idx_payment_provider", columnList = "provider"),
            @Index(name = "idx_payment_created_at", columnList = "createdAt")
        })
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(unique = true)
    String externalId; // e.g., vnp_TxnRef

    @Enumerated(EnumType.STRING)
    PaymentStatus status; // Use enum instead of magic strings

    String provider; // VNPAY, MOMO, etc.

    @Lob
    String payload; // raw payload (json or concatenated params)

    Instant createdAt;
    Instant processedAt;
}
