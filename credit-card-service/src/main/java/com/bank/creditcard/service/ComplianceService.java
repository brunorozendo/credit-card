package com.bank.creditcard.service;

import com.bank.creditcard.model.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class ComplianceService {

    private final Random random = new Random();
    
    // Mock lists for demonstration
    private final List<String> sanctionedNames = Arrays.asList(
            "SANCTIONED PERSON ONE",
            "SANCTIONED COMPANY TWO",
            "BANNED INDIVIDUAL THREE"
    );
    
    private final List<String> pepNames = Arrays.asList(
            "POLITICAL FIGURE ONE",
            "GOVERNMENT OFFICIAL TWO",
            "PUBLIC SERVANT THREE"
    );

    public ComplianceCheckResult performComplianceCheck(Customer customer) {
        log.info("Performing compliance check for customer: {} {}", 
                customer.getFirstName(), customer.getLastName());

        // Simulate API call delay
        try {
            Thread.sleep(random.nextInt(300) + 200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ComplianceCheckResult result = new ComplianceCheckResult();
        
        // KYC Check
        result.setKycPassed(performKycCheck(customer));
        
        // AML Check
        result.setAmlPassed(performAmlCheck(customer));
        
        // Sanctions Check
        result.setSanctionCheckPassed(performSanctionsCheck(customer));
        
        // PEP Check
        result.setPepCheckPassed(performPepCheck(customer));
        
        // Overall result
        result.setOverallPassed(
                result.isKycPassed() && 
                result.isAmlPassed() && 
                result.isSanctionCheckPassed() && 
                result.isPepCheckPassed()
        );

        if (!result.isOverallPassed()) {
            result.setReason(buildFailureReason(result));
        }

        return result;
    }

    private boolean performKycCheck(Customer customer) {
        // Simulate KYC verification
        // In real implementation, this would verify documents, biometrics, etc.
        return customer.isIdentityVerified() && 
               customer.getSsn() != null && 
               customer.getAddress() != null;
    }

    private boolean performAmlCheck(Customer customer) {
        // Simulate AML check
        // In real implementation, this would check transaction patterns, source of funds, etc.
        // For demo, randomly pass 95% of checks
        return random.nextDouble() > 0.05;
    }

    private boolean performSanctionsCheck(Customer customer) {
        String fullName = (customer.getFirstName() + " " + customer.getLastName()).toUpperCase();
        return sanctionedNames.stream().noneMatch(fullName::contains);
    }

    private boolean performPepCheck(Customer customer) {
        String fullName = (customer.getFirstName() + " " + customer.getLastName()).toUpperCase();
        return pepNames.stream().noneMatch(fullName::contains);
    }

    private String buildFailureReason(ComplianceCheckResult result) {
        StringBuilder reason = new StringBuilder("Compliance check failed: ");
        
        if (!result.isKycPassed()) {
            reason.append("KYC verification incomplete. ");
        }
        if (!result.isAmlPassed()) {
            reason.append("AML check failed. ");
        }
        if (!result.isSanctionCheckPassed()) {
            reason.append("Sanctions list match found. ");
        }
        if (!result.isPepCheckPassed()) {
            reason.append("PEP match found. ");
        }
        
        return reason.toString().trim();
    }

    @lombok.Data
    public static class ComplianceCheckResult {
        private boolean kycPassed;
        private boolean amlPassed;
        private boolean sanctionCheckPassed;
        private boolean pepCheckPassed;
        private boolean overallPassed;
        private String reason;
    }
}
