package com.bank.creditcard.mapper

import com.bank.creditcard.dto.AddressDto
import com.bank.creditcard.dto.CreditCardApplicationRequest
import com.bank.creditcard.dto.CreditCardApplicationResponse
import com.bank.creditcard.model.Address
import com.bank.creditcard.model.CreditCardApplication
import com.bank.creditcard.model.Customer
import org.mapstruct.factory.Mappers
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDate
import java.time.LocalDateTime

class ApplicationMapperSpec extends Specification {

    @Subject
    ApplicationMapper mapper = Mappers.getMapper(ApplicationMapper.class)

    def "should map CreditCardApplicationRequest to CreditCardApplication entity"() {
        given: "a valid application request"
        def request = createValidRequest()
        
        when: "mapping to entity"
        def entity = mapper.toEntity(request)
        
        then: "entity is mapped correctly"
        entity != null
        entity.annualIncome == request.annualIncome
        entity.employmentStatus == request.employmentStatus
        entity.requestedLimit == request.requestedLimit
        entity.cardType == CreditCardApplication.CardType.valueOf(request.cardType)
        
        and: "generated fields are null"
        entity.id == null
        entity.applicationNumber == null
        entity.status == null
        entity.customer == null
        entity.approvedLimit == null
        entity.creditScore == null
        entity.riskScore == null
        entity.decisionReason == null
        entity.createdAt == null
        entity.updatedAt == null
        entity.decidedAt == null
    }

    def "should map CreditCardApplicationRequest to Customer"() {
        given: "a valid application request"
        def request = createValidRequest()
        
        when: "mapping to customer"
        def customer = mapper.toCustomer(request)
        
        then: "customer is mapped correctly"
        customer != null
        customer.firstName == request.firstName
        customer.lastName == request.lastName
        customer.email == request.email
        customer.phoneNumber == request.phoneNumber
        customer.ssn == request.ssn
        customer.dateOfBirth == request.dateOfBirth
        
        and: "address is mapped"
        customer.address != null
        customer.address.streetAddress == request.address.streetAddress
        customer.address.city == request.address.city
        customer.address.state == request.address.state
        customer.address.zipCode == request.address.zipCode
        customer.address.country == request.address.country
        
        and: "generated fields are null/default"
        customer.id == null
        !customer.identityVerified
        customer.kycStatus == null
        customer.createdAt == null
        customer.updatedAt == null
    }

    def "should map AddressDto to Address"() {
        given: "an address DTO"
        def addressDto = new AddressDto(
                streetAddress: "123 Main St",
                city: "New York",
                state: "NY",
                zipCode: "10001",
                country: "USA"
        )
        
        when: "mapping to address"
        def address = mapper.toAddress(addressDto)
        
        then: "address is mapped correctly"
        address != null
        address.streetAddress == addressDto.streetAddress
        address.city == addressDto.city
        address.state == addressDto.state
        address.zipCode == addressDto.zipCode
        address.country == addressDto.country
    }

    def "should map CreditCardApplication to Response"() {
        given: "a credit card application entity"
        def application = createApplicationEntity()
        
        when: "mapping to response"
        def response = mapper.toResponse(application)
        
        then: "response is mapped correctly"
        response != null
        response.id == application.id
        response.applicationNumber == application.applicationNumber
        response.status == application.status.toString()
        response.customerName == "John Doe"
        response.email == application.customer.email
        response.requestedLimit == application.requestedLimit
        response.approvedLimit == application.approvedLimit
        response.cardType == application.cardType.toString()
        response.creditScore == application.creditScore
        response.riskScore == application.riskScore
        response.decisionReason == application.decisionReason
        response.createdAt == application.createdAt
        response.decidedAt == application.decidedAt
    }

    def "should handle null values in mapping"() {
        given: "an application with null optional fields"
        def application = createMinimalApplicationEntity()
        
        when: "mapping to response"
        def response = mapper.toResponse(application)
        
        then: "response handles nulls correctly"
        response != null
        response.approvedLimit == null
        response.creditScore == null
        response.riskScore == null
        response.decisionReason == null
        response.decidedAt == null
    }

    def "should concatenate customer name correctly"() {
        given: "applications with different customer names"
        def testCases = [
                ["John", "Doe", "John Doe"],
                ["Jane", "Smith", "Jane Smith"],
                ["Mary", "O'Brien", "Mary O'Brien"],
                ["José", "García", "José García"]
        ]
        
        expect: "names are concatenated correctly"
        testCases.each { firstName, lastName, expectedName ->
            def app = createApplicationEntity()
            app.customer.firstName = firstName
            app.customer.lastName = lastName
            def response = mapper.toResponse(app)
            assert response.customerName == expectedName
        }
    }

    // Helper methods
    private CreditCardApplicationRequest createValidRequest() {
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

    private CreditCardApplication createApplicationEntity() {
        def customer = new Customer(
                id: UUID.randomUUID(),
                firstName: "John",
                lastName: "Doe",
                email: "john.doe@example.com",
                phoneNumber: "+1234567890",
                ssn: "123-45-6789",
                dateOfBirth: LocalDate.of(1990, 1, 1),
                address: new Address(
                        streetAddress: "123 Main St",
                        city: "New York",
                        state: "NY",
                        zipCode: "10001",
                        country: "USA"
                ),
                identityVerified: true,
                kycStatus: Customer.KycStatus.COMPLETED
        )

        new CreditCardApplication(
                id: UUID.randomUUID(),
                applicationNumber: "APP-123456",
                status: CreditCardApplication.ApplicationStatus.APPROVED,
                customer: customer,
                requestedLimit: BigDecimal.valueOf(5000),
                approvedLimit: BigDecimal.valueOf(10000),
                annualIncome: BigDecimal.valueOf(75000),
                employmentStatus: "FULL_TIME",
                creditScore: 750,
                riskScore: BigDecimal.valueOf(25),
                cardType: CreditCardApplication.CardType.GOLD,
                decisionReason: "Approved based on credit assessment",
                createdAt: LocalDateTime.now(),
                updatedAt: LocalDateTime.now(),
                decidedAt: LocalDateTime.now()
        )
    }

    private CreditCardApplication createMinimalApplicationEntity() {
        def customer = new Customer(
                firstName: "Jane",
                lastName: "Smith",
                email: "jane.smith@example.com"
        )

        new CreditCardApplication(
                id: UUID.randomUUID(),
                applicationNumber: "APP-789012",
                status: CreditCardApplication.ApplicationStatus.PENDING,
                customer: customer,
                requestedLimit: BigDecimal.valueOf(3000),
                annualIncome: BigDecimal.valueOf(50000),
                employmentStatus: "FULL_TIME",
                cardType: CreditCardApplication.CardType.CLASSIC,
                createdAt: LocalDateTime.now()
        )
    }
}
