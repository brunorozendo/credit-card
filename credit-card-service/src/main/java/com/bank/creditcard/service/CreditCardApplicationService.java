package com.bank.creditcard.service;

import com.bank.creditcard.client.CreditBureauClient;
import com.bank.creditcard.dto.CreditBureauReport;
import com.bank.creditcard.dto.CreditCardApplicationRequest;
import com.bank.creditcard.dto.CreditCardApplicationResponse;
import com.bank.creditcard.exception.DuplicateApplicationException;
import com.bank.creditcard.exception.ResourceNotFoundException;
import com.bank.creditcard.mapper.ApplicationMapper;
import com.bank.creditcard.model.CreditCardApplication;
import com.bank.creditcard.model.Customer;
import com.bank.creditcard.repository.CreditCardApplicationRepository;
import com.bank.creditcard.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditCardApplicationService {

    private final CreditCardApplicationRepository applicationRepository;
    private final CustomerRepository customerRepository;
    private final CreditBureauClient creditBureauClient;
    private final RiskAssessmentService riskAssessmentService;
    private final ComplianceService complianceService;
    private final ApplicationMapper applicationMapper;

    @Transactional
    public CreditCardApplicationResponse submitApplication(CreditCardApplicationRequest request) {
        log.info("Processing credit card application for: {} {}", 
                request.getFirstName(), request.getLastName());

        // Check for duplicate pending applications
        if (applicationRepository.existsByCustomerSsnAndStatus(
                request.getSsn(), CreditCardApplication.ApplicationStatus.PENDING)) {
            throw new DuplicateApplicationException("A pending application already exists for this SSN");
        }

        // Create or update customer
        Customer customer = findOrCreateCustomer(request);

        // Create application
        CreditCardApplication application = applicationMapper.toEntity(request);
        application.setCustomer(customer);
        application.setStatus(CreditCardApplication.ApplicationStatus.PENDING);
        
        application = applicationRepository.save(application);
        
        // Process application asynchronously (in real implementation)
        processApplicationAsync(application.getId());

        return applicationMapper.toResponse(application);
    }

    @Async("applicationProcessorExecutor")
    @Transactional
    public void processApplicationAsync(UUID applicationId) {
        try {
            CreditCardApplication application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

            application.setStatus(CreditCardApplication.ApplicationStatus.IN_REVIEW);
            applicationRepository.save(application);

            // Step 1: Compliance Check
            ComplianceService.ComplianceCheckResult complianceResult = 
                    complianceService.performComplianceCheck(application.getCustomer());
            
            if (!complianceResult.isOverallPassed()) {
                rejectApplication(application, complianceResult.getReason());
                return;
            }

            // Step 2: Credit Bureau Check
            CreditBureauReport creditReport = creditBureauClient.getCreditReport(
                    application.getCustomer().getSsn());
            application.setCreditScore(creditReport.getCreditScore());

            // Step 3: Risk Assessment
            BigDecimal riskScore = riskAssessmentService.calculateRiskScore(application, creditReport);
            application.setRiskScore(riskScore);

            // Step 4: Decision Making
            if (creditReport.getCreditScore() < 580) {
                rejectApplication(application, "Credit score below minimum requirement (580)");
                return;
            }
            
            if (riskScore.compareTo(BigDecimal.valueOf(75)) > 0) {
                rejectApplication(application, "Risk assessment score too high (" + riskScore + "/100)");
                return;
            }

            // Step 5: Approve Application
            BigDecimal approvedLimit = riskAssessmentService.determineApprovedLimit(application, riskScore);
            approveApplication(application, approvedLimit);

        } catch (Exception e) {
            log.error("Error processing application {}: {}", applicationId, e.getMessage());
            CreditCardApplication application = applicationRepository.findById(applicationId).orElse(null);
            if (application != null) {
                rejectApplication(application, "System error during processing");
            }
        }
    }

    private Customer findOrCreateCustomer(CreditCardApplicationRequest request) {
        return customerRepository.findBySsn(request.getSsn())
                .orElseGet(() -> {
                    Customer newCustomer = applicationMapper.toCustomer(request);
                    newCustomer.setIdentityVerified(true); // Mock verification
                    newCustomer.setKycStatus(Customer.KycStatus.COMPLETED);
                    return customerRepository.save(newCustomer);
                });
    }

    private void rejectApplication(CreditCardApplication application, String reason) {
        application.setStatus(CreditCardApplication.ApplicationStatus.REJECTED);
        application.setDecisionReason(reason);
        application.setDecidedAt(LocalDateTime.now());
        applicationRepository.save(application);
        log.info("Application {} rejected: {}", application.getApplicationNumber(), reason);
    }

    private void approveApplication(CreditCardApplication application, BigDecimal approvedLimit) {
        application.setStatus(CreditCardApplication.ApplicationStatus.APPROVED);
        application.setApprovedLimit(approvedLimit);
        application.setDecisionReason("Application approved based on credit assessment");
        application.setDecidedAt(LocalDateTime.now());
        applicationRepository.save(application);
        log.info("Application {} approved with limit: ${}", 
                application.getApplicationNumber(), approvedLimit);
    }

    @Transactional(readOnly = true)
    public CreditCardApplicationResponse getApplication(String applicationNumber) {
        CreditCardApplication application = applicationRepository.findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationNumber));
        return applicationMapper.toResponse(application);
    }

    @Transactional(readOnly = true)
    public List<CreditCardApplicationResponse> getApplicationsByEmail(String email) {
        return applicationRepository.findByCustomerEmail(email).stream()
                .map(applicationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CreditCardApplicationResponse> getPendingApplications() {
        return applicationRepository.findByStatus(CreditCardApplication.ApplicationStatus.PENDING).stream()
                .map(applicationMapper::toResponse)
                .collect(Collectors.toList());
    }
}
