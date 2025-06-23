package com.bank.creditcard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Data
@EqualsAndHashCode(of = "id")
public class Customer {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "ssn", unique = true, nullable = false)
    private String ssn;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Embedded
    private Address address;

    @Column(name = "identity_verified")
    private boolean identityVerified;

    @Column(name = "kyc_status")
    @Enumerated(EnumType.STRING)
    private KycStatus kycStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        kycStatus = KycStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum KycStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }
}
