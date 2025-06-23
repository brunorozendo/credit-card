package com.bank.creditcard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CreditCardApplicationResponse {

    private UUID id;
    private String applicationNumber;
    private String status;
    private String customerName;
    private String email;
    private BigDecimal requestedLimit;
    private BigDecimal approvedLimit;
    private String cardType;
    private Integer creditScore;
    private BigDecimal riskScore;
    private String decisionReason;
    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;
}
