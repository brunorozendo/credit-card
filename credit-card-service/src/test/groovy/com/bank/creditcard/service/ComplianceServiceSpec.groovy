package com.bank.creditcard.service

import com.bank.creditcard.model.Address
import com.bank.creditcard.model.Customer
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.time.LocalDate

class ComplianceServiceSpec extends Specification {

    @Subject
    def service = new ComplianceService()

    def "should pass compliance check for valid customer"() {
        given: "a valid customer"
        def customer = createValidCustomer()
        
        when: "performing compliance check"
        def result = service.performComplianceCheck(customer)
        
        then: "all checks pass"
        result.kycPassed
        result.amlPassed
        result.sanctionCheckPassed
        result.pepCheckPassed
        result.overallPassed
        result.reason == null
    }

    def "should fail KYC check when identity not verified"() {
        given: "a customer without identity verification"
        def customer = createValidCustomer()
        customer.identityVerified = false
        
        when: "performing compliance check"
        def result = service.performComplianceCheck(customer)
        
        then: "KYC check fails"
        !result.kycPassed
        !result.overallPassed
        result.reason.contains("KYC verification incomplete")
    }

    def "should fail KYC check when SSN is missing"() {
        given: "a customer without SSN"
        def customer = createValidCustomer()
        customer.ssn = null
        
        when: "performing compliance check"
        def result = service.performComplianceCheck(customer)
        
        then: "KYC check fails"
        !result.kycPassed
        !result.overallPassed
        result.reason.contains("KYC verification incomplete")
    }

    def "should fail KYC check when address is missing"() {
        given: "a customer without address"
        def customer = createValidCustomer()
        customer.address = null
        
        when: "performing compliance check"
        def result = service.performComplianceCheck(customer)
        
        then: "KYC check fails"
        !result.kycPassed
        !result.overallPassed
        result.reason.contains("KYC verification incomplete")
    }

    @Unroll
    def "should fail sanctions check for sanctioned name: #sanctionedName"() {
        given: "a customer with sanctioned name"
        def customer = createValidCustomer()
        customer.firstName = sanctionedName.split(" ")[0]
        customer.lastName = sanctionedName.split(" ")[1]
        
        when: "performing compliance check"
        def result = service.performComplianceCheck(customer)
        
        then: "sanctions check fails"
        !result.sanctionCheckPassed
        !result.overallPassed
        result.reason.contains("Sanctions list match found")
        
        where:
        sanctionedName << ["SANCTIONED PERSON", "SANCTIONED COMPANY", "BANNED INDIVIDUAL"]
    }

    @Unroll
    def "should fail PEP check for politically exposed person: #pepName"() {
        given: "a politically exposed person"
        def customer = createValidCustomer()
        customer.firstName = pepName.split(" ")[0]
        customer.lastName = pepName.split(" ")[1]
        
        when: "performing compliance check"
        def result = service.performComplianceCheck(customer)
        
        then: "PEP check fails"
        !result.pepCheckPassed
        !result.overallPassed
        result.reason.contains("PEP match found")
        
        where:
        pepName << ["POLITICAL FIGURE", "GOVERNMENT OFFICIAL", "PUBLIC SERVANT"]
    }

    def "should build comprehensive failure reason for multiple failures"() {
        given: "a customer failing multiple checks"
        def customer = createValidCustomer()
        customer.identityVerified = false
        customer.firstName = "SANCTIONED"
        customer.lastName = "PERSON"
        
        when: "performing compliance check"
        def result = service.performComplianceCheck(customer)
        
        then: "multiple failures are reported"
        !result.kycPassed
        !result.sanctionCheckPassed
        !result.overallPassed
        result.reason.contains("KYC verification incomplete")
        result.reason.contains("Sanctions list match found")
    }

    def "should handle case-insensitive name matching"() {
        given: "a customer with sanctioned name in lowercase"
        def customer = createValidCustomer()
        customer.firstName = "sanctioned"
        customer.lastName = "person"
        
        when: "performing compliance check"
        def result = service.performComplianceCheck(customer)
        
        then: "sanctions check still fails"
        !result.sanctionCheckPassed
        !result.overallPassed
    }

    def "should handle partial name matches"() {
        given: "a customer with partial sanctioned name"
        def customer = createValidCustomer()
        customer.firstName = "John"
        customer.lastName = "SANCTIONED PERSON ONE Smith"
        
        when: "performing compliance check"
        def result = service.performComplianceCheck(customer)
        
        then: "sanctions check fails"
        !result.sanctionCheckPassed
        !result.overallPassed
    }

    // Helper methods
    private Customer createValidCustomer() {
        def customer = new Customer()
        customer.id = UUID.randomUUID()
        customer.firstName = "John"
        customer.lastName = "Doe"
        customer.email = "john.doe@example.com"
        customer.phoneNumber = "+1234567890"
        customer.ssn = "123-45-6789"
        customer.dateOfBirth = LocalDate.of(1990, 1, 1)
        customer.identityVerified = true
        customer.address = new Address(
                streetAddress: "123 Main St",
                city: "New York",
                state: "NY",
                zipCode: "10001",
                country: "USA"
        )
        customer.kycStatus = Customer.KycStatus.COMPLETED
        return customer
    }
}
