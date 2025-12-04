package com.bank.creditcard.service

import com.bank.creditcard.client.CreditBureauClient
import com.bank.creditcard.dto.CreditBureauReport
import com.bank.creditcard.dto.CreditCardApplicationRequest
import com.bank.creditcard.dto.AddressDto
import com.bank.creditcard.exception.DuplicateApplicationException
import com.bank.creditcard.mapper.ApplicationMapper
import com.bank.creditcard.model.CreditCardApplication
import com.bank.creditcard.model.Customer
import com.bank.creditcard.repository.CreditCardApplicationRepository
import com.bank.creditcard.repository.CustomerRepository
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDate

class CreditCardApplicationServiceSpec extends Specification {

    def applicationRepository = Mock(CreditCardApplicationRepository)
    def customerRepository = Mock(CustomerRepository)
    def creditBureauClient = Mock(CreditBureauClient)
    def riskAssessmentService = Mock(RiskAssessmentService)
    def complianceService = Mock(ComplianceService)
    def applicationMapper = Mock(ApplicationMapper)

    @Subject
    def service = new CreditCardApplicationService(
            applicationRepository,
            customerRepository,
            creditBureauClient,
            riskAssessmentService,
            complianceService,
            applicationMapper
    )

    def "should submit credit card application successfully"() {
        given: "a valid application request"
        def request = createValidApplicationRequest()
        and: "Setup a new customer and application"
        def customer = createCustomer()
        def application = createApplication(customer)
        
        when: "submitting the application"
        def response = service.submitApplication(request)
        
        then: "check for duplicate applications"
        1 * applicationRepository.existsByCustomerSsnAndStatus(request.ssn, CreditCardApplication.ApplicationStatus.PENDING) >> false
        
        and: "find or create customer"
        1 * customerRepository.findBySsn(request.ssn) >> Optional.empty()
        1 * applicationMapper.toCustomer(request) >> customer
        1 * customerRepository.save(_ as Customer) >> customer
        
        and: "create and save application"
        1 * applicationMapper.toEntity(request) >> application
        1 * applicationRepository.save(_ as CreditCardApplication) >> application
        1 * applicationMapper.toResponse(application) >> _
        
        and: "response is returned"
        response != null
    }

    def "should throw exception for duplicate pending application"() {
        given: "a duplicate application request"
        def request = createValidApplicationRequest()
        
        when: "submitting the duplicate application"
        service.submitApplication(request)
        
        then: "duplicate check returns true"
        1 * applicationRepository.existsByCustomerSsnAndStatus(request.ssn, CreditCardApplication.ApplicationStatus.PENDING) >> true
        
        and: "exception is thrown"
        thrown(DuplicateApplicationException)
    }

    def "should process application with approval"() {
        given: "an application to process"
        def applicationId = UUID.randomUUID()
        def application = createApplicationForProcessing()
        def creditReport = createCreditReport(750)
        def complianceResult = new ComplianceService.ComplianceCheckResult(
                overallPassed: true,
                kycPassed: true,
                amlPassed: true,
                sanctionCheckPassed: true,
                pepCheckPassed: true
        )
        
        when: "processing the application"
        service.processApplicationAsync(applicationId)
        
        then: "application is retrieved and updated"
        1 * applicationRepository.findById(applicationId) >> Optional.of(application)
        1 * applicationRepository.save({ it.status == CreditCardApplication.ApplicationStatus.IN_REVIEW })
        
        and: "compliance check passes"
        1 * complianceService.performComplianceCheck(application.customer) >> complianceResult
        
        and: "credit bureau report is fetched"
        1 * creditBureauClient.getCreditReport(application.customer.ssn) >> creditReport
        
        and: "risk assessment is performed"
        1 * riskAssessmentService.calculateRiskScore(application, creditReport) >> BigDecimal.valueOf(25)
        1 * riskAssessmentService.determineApprovedLimit(application, BigDecimal.valueOf(25)) >> BigDecimal.valueOf(10000)
        
        and: "application is approved"
        1 * applicationRepository.save({ 
            it.status == CreditCardApplication.ApplicationStatus.APPROVED &&
            it.approvedLimit == BigDecimal.valueOf(10000)
        })
    }

    def "should reject application due to low credit score"() {
        given: "an application with low credit score"
        def applicationId = UUID.randomUUID()
        def application = createApplicationForProcessing()
        def creditReport = createCreditReport(550) // Low credit score
        def complianceResult = new ComplianceService.ComplianceCheckResult(overallPassed: true)
        
        when: "processing the application"
        service.processApplicationAsync(applicationId)
        
        then: "application process starts"
        1 * applicationRepository.findById(applicationId) >> Optional.of(application)
        1 * applicationRepository.save({ it.status == CreditCardApplication.ApplicationStatus.IN_REVIEW })
        1 * complianceService.performComplianceCheck(application.customer) >> complianceResult
        1 * creditBureauClient.getCreditReport(application.customer.ssn) >> creditReport
        1 * riskAssessmentService.calculateRiskScore(application, creditReport) >> BigDecimal.valueOf(85)
        
        and: "application is rejected"
        1 * applicationRepository.save({ 
            it.status == CreditCardApplication.ApplicationStatus.REJECTED &&
            it.decisionReason.contains("Credit score or risk assessment")
        })
    }

    // Helper methods
    private CreditCardApplicationRequest createValidApplicationRequest() {
        new CreditCardApplicationRequest(
                firstName: "John",
                lastName: "Doe",
                email: "john.doe@example.com",
                phoneNumber: "+1234567890",
                ssn: "123-45-6789",
                dateOfBirth: LocalDate.of(1990, 1, 1),
                address: new AddressDto(
                        streetAddress: "123 Main St",
                        city: "New York",
                        state: "NY",
                        zipCode: "10001",
                        country: "USA"
                ),
                annualIncome: BigDecimal.valueOf(75000),
                employmentStatus: "FULL_TIME",
                requestedLimit: BigDecimal.valueOf(5000),
                cardType: "GOLD"
        )
    }

    private Customer createCustomer() {
        new Customer(
                id: UUID.randomUUID(),
                firstName: "John",
                lastName: "Doe",
                email: "john.doe@example.com",
                phoneNumber: "+1234567890",
                ssn: "123-45-6789",
                dateOfBirth: LocalDate.of(1990, 1, 1),
                identityVerified: true,
                kycStatus: Customer.KycStatus.COMPLETED
        )
    }

    private CreditCardApplication createApplication(Customer customer) {
        new CreditCardApplication(
                id: UUID.randomUUID(),
                applicationNumber: "APP-123456",
                status: CreditCardApplication.ApplicationStatus.PENDING,
                customer: customer,
                requestedLimit: BigDecimal.valueOf(5000),
                annualIncome: BigDecimal.valueOf(75000),
                employmentStatus: "FULL_TIME",
                cardType: CreditCardApplication.CardType.GOLD
        )
    }

    private CreditCardApplication createApplicationForProcessing() {
        def customer = createCustomer()
        createApplication(customer)
    }

    private CreditBureauReport createCreditReport(int creditScore) {
        CreditBureauReport.builder()
                .ssn("123-45-6789")
                .creditScore(creditScore)
                .totalDebt(BigDecimal.valueOf(15000))
                .monthlyDebtPayments(BigDecimal.valueOf(500))
                .numberOfAccounts(5)
                .numberOfDelinquentAccounts(0)
                .creditAccounts([])
                .recentInquiries([])
                .reportDate(LocalDate.now())
                .build()
    }
}
