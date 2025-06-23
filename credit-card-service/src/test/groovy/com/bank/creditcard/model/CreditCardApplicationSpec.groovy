package com.bank.creditcard.model

import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime

class CreditCardApplicationSpec extends Specification {

    def "should generate application number on creation"() {
        given: "a new application"
        def application = new CreditCardApplication()
        
        when: "onCreate is called"
        application.onCreate()
        
        then: "application number and created date are set"
        application.applicationNumber != null
        application.applicationNumber.startsWith("APP-")
        application.createdAt != null
    }

    def "should update timestamp on update"() {
        given: "an existing application"
        def application = new CreditCardApplication()
        application.onCreate()
        def originalCreatedAt = application.createdAt
        
        when: "onUpdate is called"
        Thread.sleep(10) // Ensure time difference
        application.onUpdate()
        
        then: "updated date is set but created date unchanged"
        application.updatedAt != null
        application.createdAt == originalCreatedAt
        application.updatedAt.isAfter(originalCreatedAt)
    }

    def "should handle equals and hashCode correctly"() {
        given: "two applications"
        def app1 = new CreditCardApplication()
        def app2 = new CreditCardApplication()
        def uuid = UUID.randomUUID()
        
        when: "both have same id"
        app1.id = uuid
        app2.id = uuid
        
        then: "they are equal"
        app1 == app2
        app1.hashCode() == app2.hashCode()
        
        when: "ids are different"
        app2.id = UUID.randomUUID()
        
        then: "they are not equal"
        app1 != app2
        app1.hashCode() != app2.hashCode()
        
        when: "one id is null"
        app1.id = null
        
        then: "they are not equal"
        app1 != app2
    }

    def "should have proper toString excluding customer"() {
        given: "an application with customer"
        def customer = new Customer(firstName: "John", lastName: "Doe")
        def application = new CreditCardApplication(
                id: UUID.randomUUID(),
                applicationNumber: "APP-123",
                status: CreditCardApplication.ApplicationStatus.PENDING,
                customer: customer
        )
        
        when: "calling toString"
        def result = application.toString()
        
        then: "customer is not included to avoid circular reference"
        result.contains("APP-123")
        result.contains("PENDING")
        !result.contains("John")
        !result.contains("Doe")
    }

    def "should handle all enum values"() {
        when: "using ApplicationStatus enum"
        def statuses = CreditCardApplication.ApplicationStatus.values()
        
        then: "all statuses are available"
        statuses.length == 5
        statuses*.name() == ["PENDING", "IN_REVIEW", "APPROVED", "REJECTED", "CANCELLED"]
        
        when: "using CardType enum"
        def cardTypes = CreditCardApplication.CardType.values()
        
        then: "all card types are available"
        cardTypes.length == 4
        cardTypes*.name() == ["CLASSIC", "GOLD", "PLATINUM", "INFINITE"]
    }

    def "should generate unique application numbers"() {
        given: "multiple applications"
        def applications = (1..10).collect { new CreditCardApplication() }
        
        when: "onCreate is called for each"
        applications.each { it.onCreate() }
        def applicationNumbers = applications*.applicationNumber
        
        then: "all application numbers are unique"
        applicationNumbers.unique().size() == 10
        applicationNumbers.every { it.startsWith("APP-") }
    }

    def "should handle null values in fields"() {
        given: "an application with null fields"
        def application = new CreditCardApplication()
        
        expect: "null fields are handled"
        application.id == null
        application.applicationNumber == null
        application.status == null
        application.customer == null
        application.requestedLimit == null
        application.approvedLimit == null
        application.creditScore == null
        application.riskScore == null
        application.cardType == null
        application.decisionReason == null
        application.createdAt == null
        application.updatedAt == null
        application.decidedAt == null
    }

    def "should set all fields correctly"() {
        given: "field values"
        def id = UUID.randomUUID()
        def now = LocalDateTime.now()
        def customer = new Customer()
        
        when: "setting all fields"
        def application = new CreditCardApplication(
                id: id,
                applicationNumber: "APP-TEST-123",
                status: CreditCardApplication.ApplicationStatus.APPROVED,
                customer: customer,
                requestedLimit: BigDecimal.valueOf(5000),
                approvedLimit: BigDecimal.valueOf(10000),
                annualIncome: BigDecimal.valueOf(75000),
                employmentStatus: "FULL_TIME",
                creditScore: 750,
                riskScore: BigDecimal.valueOf(25.5),
                cardType: CreditCardApplication.CardType.GOLD,
                decisionReason: "Approved based on excellent credit",
                createdAt: now,
                updatedAt: now.plusHours(1),
                decidedAt: now.plusHours(2)
        )
        
        then: "all fields are set correctly"
        application.id == id
        application.applicationNumber == "APP-TEST-123"
        application.status == CreditCardApplication.ApplicationStatus.APPROVED
        application.customer == customer
        application.requestedLimit == BigDecimal.valueOf(5000)
        application.approvedLimit == BigDecimal.valueOf(10000)
        application.annualIncome == BigDecimal.valueOf(75000)
        application.employmentStatus == "FULL_TIME"
        application.creditScore == 750
        application.riskScore == BigDecimal.valueOf(25.5)
        application.cardType == CreditCardApplication.CardType.GOLD
        application.decisionReason == "Approved based on excellent credit"
        application.createdAt == now
        application.updatedAt == now.plusHours(1)
        application.decidedAt == now.plusHours(2)
    }
}
