package com.bank.creditcard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class CreditBureauReport {

    private String ssn;
    private Integer creditScore;
    private BigDecimal totalDebt;
    private BigDecimal monthlyDebtPayments;
    private Integer numberOfAccounts;
    private Integer numberOfDelinquentAccounts;
    private List<CreditAccount> creditAccounts;
    private List<CreditInquiry> recentInquiries;
    private LocalDate reportDate;

    @Data
    @Builder
    public static class CreditAccount {
        private String accountType;
        private String creditorName;
        private BigDecimal balance;
        private BigDecimal creditLimit;
        private BigDecimal monthlyPayment;
        private String status;
        private LocalDate openDate;
    }

    @Data
    @Builder
    public static class CreditInquiry {
        private String inquirerName;
        private LocalDate inquiryDate;
        private String inquiryType;
    }
}
