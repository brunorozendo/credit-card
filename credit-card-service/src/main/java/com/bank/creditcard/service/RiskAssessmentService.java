package com.bank.creditcard.service;

import com.bank.creditcard.dto.CreditBureauReport;
import com.bank.creditcard.model.CreditCardApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
public class RiskAssessmentService {

    public BigDecimal calculateRiskScore(CreditCardApplication application, CreditBureauReport creditReport) {
        log.info("Calculating risk score for application: {}", application.getApplicationNumber());

        // Risk score components (0-100 scale, lower is better)
        BigDecimal creditScoreRisk = calculateCreditScoreRisk(creditReport.getCreditScore());
        BigDecimal dtiRisk = calculateDTIRisk(application.getAnnualIncome(), creditReport.getMonthlyDebtPayments());
        BigDecimal delinquencyRisk = calculateDelinquencyRisk(creditReport);
        BigDecimal inquiryRisk = calculateInquiryRisk(creditReport);
        BigDecimal utilizationRisk = calculateUtilizationRisk(creditReport);

        // Weighted average
        BigDecimal totalRisk = creditScoreRisk.multiply(BigDecimal.valueOf(0.35))
                .add(dtiRisk.multiply(BigDecimal.valueOf(0.25)))
                .add(delinquencyRisk.multiply(BigDecimal.valueOf(0.20)))
                .add(utilizationRisk.multiply(BigDecimal.valueOf(0.15)))
                .add(inquiryRisk.multiply(BigDecimal.valueOf(0.05)));

        return totalRisk.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCreditScoreRisk(Integer creditScore) {
        if (creditScore >= 800) return BigDecimal.valueOf(5);
        if (creditScore >= 740) return BigDecimal.valueOf(15);
        if (creditScore >= 670) return BigDecimal.valueOf(30);
        if (creditScore >= 580) return BigDecimal.valueOf(60);
        return BigDecimal.valueOf(90);
    }

    private BigDecimal calculateDTIRisk(BigDecimal annualIncome, BigDecimal monthlyDebtPayments) {
        BigDecimal monthlyIncome = annualIncome.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        
        if (monthlyIncome.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        
        BigDecimal dtiRatio = monthlyDebtPayments.divide(monthlyIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        if (dtiRatio.compareTo(BigDecimal.valueOf(20)) <= 0) return BigDecimal.valueOf(10);
        if (dtiRatio.compareTo(BigDecimal.valueOf(30)) <= 0) return BigDecimal.valueOf(25);
        if (dtiRatio.compareTo(BigDecimal.valueOf(40)) <= 0) return BigDecimal.valueOf(50);
        if (dtiRatio.compareTo(BigDecimal.valueOf(50)) <= 0) return BigDecimal.valueOf(75);
        return BigDecimal.valueOf(95);
    }

    private BigDecimal calculateDelinquencyRisk(CreditBureauReport report) {
        if (report.getNumberOfDelinquentAccounts() == 0) return BigDecimal.valueOf(5);
        if (report.getNumberOfDelinquentAccounts() == 1) return BigDecimal.valueOf(40);
        if (report.getNumberOfDelinquentAccounts() == 2) return BigDecimal.valueOf(70);
        return BigDecimal.valueOf(95);
    }

    private BigDecimal calculateInquiryRisk(CreditBureauReport report) {
        int recentInquiries = report.getRecentInquiries().size();
        if (recentInquiries <= 1) return BigDecimal.valueOf(10);
        if (recentInquiries <= 3) return BigDecimal.valueOf(30);
        if (recentInquiries <= 5) return BigDecimal.valueOf(60);
        return BigDecimal.valueOf(85);
    }

    private BigDecimal calculateUtilizationRisk(CreditBureauReport report) {
        BigDecimal totalBalance = BigDecimal.ZERO;
        BigDecimal totalLimit = BigDecimal.ZERO;

        for (CreditBureauReport.CreditAccount account : report.getCreditAccounts()) {
            if ("Credit Card".equals(account.getAccountType())) {
                totalBalance = totalBalance.add(account.getBalance());
                totalLimit = totalLimit.add(account.getCreditLimit());
            }
        }

        if (totalLimit.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(50); // No credit card history
        }

        BigDecimal utilization = totalBalance.divide(totalLimit, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        if (utilization.compareTo(BigDecimal.valueOf(10)) <= 0) return BigDecimal.valueOf(5);
        if (utilization.compareTo(BigDecimal.valueOf(30)) <= 0) return BigDecimal.valueOf(20);
        if (utilization.compareTo(BigDecimal.valueOf(50)) <= 0) return BigDecimal.valueOf(45);
        if (utilization.compareTo(BigDecimal.valueOf(70)) <= 0) return BigDecimal.valueOf(70);
        return BigDecimal.valueOf(90);
    }

    public BigDecimal determineApprovedLimit(CreditCardApplication application, BigDecimal riskScore) {
        BigDecimal baseLimit = application.getAnnualIncome().multiply(BigDecimal.valueOf(0.2));
        BigDecimal riskMultiplier = BigDecimal.valueOf(100).subtract(riskScore)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        BigDecimal calculatedLimit = baseLimit.multiply(riskMultiplier);
        BigDecimal requestedLimit = application.getRequestedLimit();

        // Return the lower of calculated or requested limit
        BigDecimal approvedLimit = calculatedLimit.min(requestedLimit);

        // Round to nearest $500
        return approvedLimit.divide(BigDecimal.valueOf(500), 0, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(500));
    }
}
