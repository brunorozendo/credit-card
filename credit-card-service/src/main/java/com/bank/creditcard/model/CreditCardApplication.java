package com.bank.creditcard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "credit_card_applications")
@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"customer"})
public class CreditCardApplication {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(name = "application_number", unique = true, nullable = false)
    private String applicationNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "requested_limit")
    private BigDecimal requestedLimit;

    @Column(name = "approved_limit")
    private BigDecimal approvedLimit;

    @Column(name = "annual_income", nullable = false)
    private BigDecimal annualIncome;

    @Column(name = "employment_status", nullable = false)
    private String employmentStatus;

    @Column(name = "credit_score")
    private Integer creditScore;

    @Column(name = "risk_score")
    private BigDecimal riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type")
    private CardType cardType;

    @Column(name = "decision_reason")
    private String decisionReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        applicationNumber = generateApplicationNumber();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generateApplicationNumber() {
        return "APP-" + System.currentTimeMillis();
    }

    public enum ApplicationStatus {
        PENDING,
        IN_REVIEW,
        APPROVED,
        REJECTED,
        CANCELLED
    }

    public enum CardType {
        CLASSIC,
        GOLD,
        PLATINUM,
        INFINITE
    }
}
