package com.bank.creditcard.client;

import com.bank.creditcard.dto.CreditBureauReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class CreditBureauClient {

    private final Random random = new Random();

    public CreditBureauReport getCreditReport(String ssn) {
        log.info("Fetching credit report for SSN: {}", maskSsn(ssn));
        
        // Simulate API call delay
        try {
            Thread.sleep(random.nextInt(500) + 500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Generate mock credit report
        return generateMockCreditReport(ssn);
    }

    private CreditBureauReport generateMockCreditReport(String ssn) {
        // Generate credit scores with better distribution for testing
        // 70% chance of good score (650-850), 20% medium (580-649), 10% poor (300-579)
        int creditScore;
        double rand = random.nextDouble();
        if (rand < 0.7) {
            creditScore = 650 + random.nextInt(201); // 650-850
        } else if (rand < 0.9) {
            creditScore = 580 + random.nextInt(70); // 580-649
        } else {
            creditScore = 300 + random.nextInt(280); // 300-579
        }
        
        List<CreditBureauReport.CreditAccount> accounts = generateMockAccounts();
        BigDecimal totalDebt = calculateTotalDebt(accounts);
        BigDecimal monthlyPayments = calculateMonthlyPayments(accounts);
        
        return CreditBureauReport.builder()
                .ssn(ssn)
                .creditScore(creditScore)
                .totalDebt(totalDebt)
                .monthlyDebtPayments(monthlyPayments)
                .numberOfAccounts(accounts.size())
                .numberOfDelinquentAccounts(random.nextInt(Math.max(1, accounts.size() / 4)))
                .creditAccounts(accounts)
                .recentInquiries(generateMockInquiries())
                .reportDate(LocalDate.now())
                .build();
    }

    private List<CreditBureauReport.CreditAccount> generateMockAccounts() {
        List<CreditBureauReport.CreditAccount> accounts = new ArrayList<>();
        int numberOfAccounts = random.nextInt(5) + 1;
        
        String[] accountTypes = {"Credit Card", "Auto Loan", "Mortgage", "Personal Loan", "Student Loan"};
        String[] creditors = {"Bank of America", "Chase", "Wells Fargo", "Capital One", "Discover"};
        String[] statuses = {"Current", "Current", "Current", "30 Days Late", "60 Days Late"};
        
        for (int i = 0; i < numberOfAccounts; i++) {
            String accountType = accountTypes[random.nextInt(accountTypes.length)];
            BigDecimal creditLimit = BigDecimal.valueOf(random.nextInt(20000) + 1000);
            // Keep balances lower for better DTI ratios (0-50% of limit)
            BigDecimal balance = creditLimit.multiply(BigDecimal.valueOf(random.nextDouble() * 0.5));
            
            accounts.add(CreditBureauReport.CreditAccount.builder()
                    .accountType(accountType)
                    .creditorName(creditors[random.nextInt(creditors.length)])
                    .balance(balance)
                    .creditLimit(creditLimit)
                    .monthlyPayment(balance.multiply(BigDecimal.valueOf(0.02)))
                    .status(statuses[random.nextInt(statuses.length)])
                    .openDate(LocalDate.now().minusMonths(random.nextInt(120)))
                    .build());
        }
        
        return accounts;
    }

    private List<CreditBureauReport.CreditInquiry> generateMockInquiries() {
        List<CreditBureauReport.CreditInquiry> inquiries = new ArrayList<>();
        int numberOfInquiries = random.nextInt(4);
        
        String[] inquirers = {"Target", "Best Buy", "Amazon Store Card", "Home Depot"};
        
        for (int i = 0; i < numberOfInquiries; i++) {
            inquiries.add(CreditBureauReport.CreditInquiry.builder()
                    .inquirerName(inquirers[random.nextInt(inquirers.length)])
                    .inquiryDate(LocalDate.now().minusDays(random.nextInt(90)))
                    .inquiryType("Hard Inquiry")
                    .build());
        }
        
        return inquiries;
    }

    private BigDecimal calculateTotalDebt(List<CreditBureauReport.CreditAccount> accounts) {
        return accounts.stream()
                .map(CreditBureauReport.CreditAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateMonthlyPayments(List<CreditBureauReport.CreditAccount> accounts) {
        return accounts.stream()
                .map(CreditBureauReport.CreditAccount::getMonthlyPayment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String maskSsn(String ssn) {
        if (ssn == null || ssn.length() < 4) {
            return "***-**-****";
        }
        return "***-**-" + ssn.substring(ssn.length() - 4);
    }
}
