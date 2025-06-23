package com.bank.creditcard.model

import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime

class CustomerSpec extends Specification {

    def "should set default values on creation"() {
        given: "a new customer"
        def customer = new Customer()
        
        when: "onCreate is called"
        customer.onCreate()
        
        then: "default values are set"
        customer.createdAt != null
        customer.kycStatus == Customer.KycStatus.PENDING
    }

    def "should update timestamp on update"() {
        given: "an existing customer"
        def customer = new Customer()
        customer.onCreate()
        def originalCreatedAt = customer.createdAt
        
        when: "onUpdate is called"
        Thread.sleep(10) // Ensure time difference
        customer.onUpdate()
        
        then: "updated date is set but created date unchanged"
        customer.updatedAt != null
        customer.createdAt == originalCreatedAt
        customer.updatedAt.isAfter(originalCreatedAt)
    }

    def "should handle equals and hashCode based on id"() {
        given: "two customers"
        def customer1 = new Customer()
        def customer2 = new Customer()
        def uuid = UUID.randomUUID()
        
        when: "both have same id"
        customer1.id = uuid
        customer2.id = uuid
        
        then: "they are equal"
        customer1 == customer2
        customer1.hashCode() == customer2.hashCode()
        
        when: "ids are different"
        customer2.id = UUID.randomUUID()
        
        then: "they are not equal"
        customer1 != customer2
        customer1.hashCode() != customer2.hashCode()
        
        when: "comparing with null id"
        customer1.id = null
        
        then: "they are not equal"
        customer1 != customer2
    }

    def "should handle all KYC status values"() {
        when: "using KycStatus enum"
        def statuses = Customer.KycStatus.values()
        
        then: "all statuses are available"
        statuses.length == 4
        statuses*.name() == ["PENDING", "IN_PROGRESS", "COMPLETED", "FAILED"]
    }

    def "should handle embedded address correctly"() {
        given: "a customer with address"
        def address = new Address(
                streetAddress: "123 Main St",
                city: "New York",
                state: "NY",
                zipCode: "10001",
                country: "USA"
        )
        def customer = new Customer(address: address)
        
        expect: "address is properly embedded"
        customer.address == address
        customer.address.streetAddress == "123 Main St"
        customer.address.city == "New York"
    }

    def "should handle null values in fields"() {
        given: "a customer with null fields"
        def customer = new Customer()
        
        expect: "null fields are handled"
        customer.id == null
        customer.firstName == null
        customer.lastName == null
        customer.email == null
        customer.phoneNumber == null
        customer.ssn == null
        customer.dateOfBirth == null
        customer.address == null
        !customer.identityVerified
        customer.kycStatus == null
        customer.createdAt == null
        customer.updatedAt == null
    }

    def "should set all fields correctly"() {
        given: "field values"
        def id = UUID.randomUUID()
        def now = LocalDateTime.now()
        def dob = LocalDate.of(1990, 1, 15)
        def address = new Address(
                streetAddress: "456 Oak Ave",
                city: "Los Angeles",
                state: "CA",
                zipCode: "90001",
                country: "USA"
        )
        
        when: "setting all fields"
        def customer = new Customer(
                id: id,
                firstName: "Jane",
                lastName: "Smith",
                email: "jane.smith@example.com",
                phoneNumber: "+19876543210",
                ssn: "987-65-4321",
                dateOfBirth: dob,
                address: address,
                identityVerified: true,
                kycStatus: Customer.KycStatus.COMPLETED,
                createdAt: now,
                updatedAt: now.plusDays(1)
        )
        
        then: "all fields are set correctly"
        customer.id == id
        customer.firstName == "Jane"
        customer.lastName == "Smith"
        customer.email == "jane.smith@example.com"
        customer.phoneNumber == "+19876543210"
        customer.ssn == "987-65-4321"
        customer.dateOfBirth == dob
        customer.address == address
        customer.identityVerified == true
        customer.kycStatus == Customer.KycStatus.COMPLETED
        customer.createdAt == now
        customer.updatedAt == now.plusDays(1)
    }

    def "should handle toString method"() {
        given: "a customer with data"
        def customer = new Customer(
                id: UUID.randomUUID(),
                firstName: "Test",
                lastName: "User",
                email: "test@example.com"
        )
        
        when: "calling toString"
        def result = customer.toString()
        
        then: "string representation includes key fields"
        result != null
        result.contains("Test")
        result.contains("User")
        result.contains("test@example.com")
    }

    def "should handle customer age calculation"() {
        given: "customers with different birth dates"
        def today = LocalDate.now()
        def customer18 = new Customer(dateOfBirth: today.minusYears(18))
        def customer65 = new Customer(dateOfBirth: today.minusYears(65))
        def customerYoung = new Customer(dateOfBirth: today.minusYears(17))
        
        when: "calculating ages"
        def age18 = java.time.Period.between(customer18.dateOfBirth, today).years
        def age65 = java.time.Period.between(customer65.dateOfBirth, today).years
        def ageYoung = java.time.Period.between(customerYoung.dateOfBirth, today).years
        
        then: "ages are calculated correctly"
        age18 == 18
        age65 == 65
        ageYoung == 17
    }
}
