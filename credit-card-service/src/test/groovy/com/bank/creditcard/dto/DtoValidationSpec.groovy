package com.bank.creditcard.dto

import jakarta.validation.Validation
import jakarta.validation.Validator
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate
import java.time.LocalDateTime

class DtoValidationSpec extends Specification {

    Validator validator = Validation.buildDefaultValidatorFactory().getValidator()

    def "should validate valid CreditCardApplicationRequest"() {
        given: "a valid request"
        def request = createValidRequest()
        
        when: "validating"
        def violations = validator.validate(request)
        
        then: "no violations"
        violations.isEmpty()
    }

    @Unroll
    def "should fail validation when #field is #value"() {
        given: "a request with invalid field"
        def request = createValidRequest()
        request."$field" = value
        
        when: "validating"
        def violations = validator.validate(request)
        
        then: "validation fails"
        !violations.isEmpty()
        violations.any { it.propertyPath.toString().contains(field) || it.propertyPath.toString() == field }
        
        where:
        field              | value
        "firstName"        | null
        "firstName"        | ""
        "firstName"        | "   "
        "lastName"         | null
        "lastName"         | ""
        "email"            | null
        "email"            | ""
        "email"            | "invalid-email"
        "email"            | "@example.com"
        "phoneNumber"      | "123"
        "phoneNumber"      | "invalid-phone"
        "ssn"              | "123456789"
        "ssn"              | "12-34-5678"
        "dateOfBirth"      | null
        "dateOfBirth"      | LocalDate.now().plusDays(1)
        "address"          | null
        "annualIncome"     | null
        "annualIncome"     | BigDecimal.valueOf(-1000)
        "employmentStatus" | null
        "employmentStatus" | ""
        "requestedLimit"   | BigDecimal.valueOf(500)
        "requestedLimit"   | BigDecimal.valueOf(150000)
        "cardType"         | null
    }

    def "should validate AddressDto"() {
        given: "a valid address"
        def address = createValidAddress()
        
        when: "validating"
        def violations = validator.validate(address)
        
        then: "no violations"
        violations.isEmpty()
    }

    @Unroll
    def "should fail address validation when #field is #value"() {
        given: "an address with invalid field"
        def address = createValidAddress()
        address."$field" = value
        
        when: "validating"
        def violations = validator.validate(address)
        
        then: "validation fails"
        !violations.isEmpty()
        
        where:
        field          | value
        "streetAddress"| null
        "streetAddress"| ""
        "city"         | null
        "city"         | ""
        "state"        | null
        "state"        | ""
        "state"        | "N"
        "state"        | "NYC"
        "state"        | "ny"
        "zipCode"      | null
        "zipCode"      | ""
        "zipCode"      | "1234"
        "zipCode"      | "123456"
        "zipCode"      | "ABCDE"
        "country"      | null
        "country"      | ""
    }

    def "should accept valid zip code formats"() {
        given: "addresses with different zip formats"
        def validZips = ["12345", "12345-6789", "00501", "99950-0000"]
        
        expect: "all valid formats pass"
        validZips.each { zip ->
            def address = createValidAddress()
            address.zipCode = zip
            def violations = validator.validate(address)
            assert violations.isEmpty()
        }
    }

    def "should validate CreditCardApplicationResponse builder"() {
        when: "building a response"
        def response = CreditCardApplicationResponse.builder()
                .id(UUID.randomUUID())
                .applicationNumber("APP-123456")
                .status("APPROVED")
                .customerName("John Doe")
                .email("john@example.com")
                .requestedLimit(BigDecimal.valueOf(5000))
                .approvedLimit(BigDecimal.valueOf(10000))
                .cardType("GOLD")
                .creditScore(750)
                .riskScore(BigDecimal.valueOf(25))
                .decisionReason("Approved")
                .createdAt(LocalDateTime.now())
                .decidedAt(LocalDateTime.now())
                .build()
        
        then: "all fields are set"
        response.id != null
        response.applicationNumber == "APP-123456"
        response.status == "APPROVED"
        response.customerName == "John Doe"
        response.email == "john@example.com"
        response.requestedLimit == BigDecimal.valueOf(5000)
        response.approvedLimit == BigDecimal.valueOf(10000)
        response.cardType == "GOLD"
        response.creditScore == 750
        response.riskScore == BigDecimal.valueOf(25)
        response.decisionReason == "Approved"
        response.createdAt != null
        response.decidedAt != null
    }

    def "should build CreditBureauReport with nested builders"() {
        when: "building a credit bureau report"
        def report = CreditBureauReport.builder()
                .ssn("123-45-6789")
                .creditScore(750)
                .totalDebt(BigDecimal.valueOf(25000))
                .monthlyDebtPayments(BigDecimal.valueOf(1500))
                .numberOfAccounts(5)
                .numberOfDelinquentAccounts(0)
                .creditAccounts([
                        CreditBureauReport.CreditAccount.builder()
                                .accountType("Credit Card")
                                .creditorName("Chase")
                                .balance(BigDecimal.valueOf(2000))
                                .creditLimit(BigDecimal.valueOf(10000))
                                .monthlyPayment(BigDecimal.valueOf(100))
                                .status("Current")
                                .openDate(LocalDate.now().minusYears(2))
                                .build()
                ])
                .recentInquiries([
                        CreditBureauReport.CreditInquiry.builder()
                                .inquirerName("Target")
                                .inquiryDate(LocalDate.now().minusDays(30))
                                .inquiryType("Hard Inquiry")
                                .build()
                ])
                .reportDate(LocalDate.now())
                .build()
        
        then: "report is built correctly"
        report.ssn == "123-45-6789"
        report.creditScore == 750
        report.creditAccounts.size() == 1
        report.creditAccounts[0].accountType == "Credit Card"
        report.recentInquiries.size() == 1
        report.recentInquiries[0].inquirerName == "Target"
    }

    def "should handle toString methods"() {
        given: "various DTOs"
        def request = createValidRequest()
        def response = CreditCardApplicationResponse.builder()
                .applicationNumber("APP-123")
                .status("PENDING")
                .build()
        def address = createValidAddress()
        
        expect: "toString returns non-null"
        request.toString() != null
        response.toString() != null
        address.toString() != null
    }

    def "should handle equals and hashCode"() {
        given: "DTOs with same values"
        def request1 = createValidRequest()
        def request2 = createValidRequest()
        
        def address1 = createValidAddress()
        def address2 = createValidAddress()
        
        expect: "equals and hashCode work correctly"
        request1 == request2
        request1.hashCode() == request2.hashCode()
        
        address1 == address2
        address1.hashCode() == address2.hashCode()
    }

    // Helper methods
    private CreditCardApplicationRequest createValidRequest() {
        def request = new CreditCardApplicationRequest()
        request.firstName = "John"
        request.lastName = "Doe"
        request.email = "john.doe@example.com"
        request.phoneNumber = "+1234567890"
        request.ssn = "123-45-6789"
        request.dateOfBirth = LocalDate.of(1990, 1, 1)
        request.address = createValidAddress()
        request.annualIncome = BigDecimal.valueOf(75000)
        request.employmentStatus = "FULL_TIME"
        request.requestedLimit = BigDecimal.valueOf(5000)
        request.cardType = "GOLD"
        return request
    }

    private AddressDto createValidAddress() {
        def address = new AddressDto()
        address.streetAddress = "123 Main St"
        address.city = "New York"
        address.state = "NY"
        address.zipCode = "10001"
        address.country = "USA"
        return address
    }
}
