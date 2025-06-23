package com.bank.creditcard.repository

import com.bank.creditcard.model.Address
import com.bank.creditcard.model.Customer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import jakarta.persistence.PersistenceException
import java.time.LocalDate

@DataJpaTest
@ActiveProfiles("test")
class CustomerRepositorySpec extends Specification {

    @Autowired
    TestEntityManager entityManager

    @Autowired
    CustomerRepository repository

    def "should save and find customer by id"() {
        given: "a customer"
        def customer = createCustomer()
        
        when: "saving the customer"
        def saved = repository.save(customer)
        entityManager.flush()
        entityManager.clear()
        
        then: "customer is saved with generated id"
        saved.id != null
        saved.createdAt != null
        
        when: "finding by id"
        def found = repository.findById(saved.id)
        
        then: "customer is found"
        found.isPresent()
        found.get().email == customer.email
    }

    def "should find customer by SSN"() {
        given: "a customer with SSN"
        def customer = createCustomer("111-11-1111", "test1@example.com")
        repository.save(customer)
        entityManager.flush()
        
        when: "finding by SSN"
        def found = repository.findBySsn("111-11-1111")
        
        then: "customer is found"
        found.isPresent()
        found.get().ssn == "111-11-1111"
    }

    def "should find customer by email"() {
        given: "a customer with email"
        def customer = createCustomer("222-22-2222", "unique@example.com")
        repository.save(customer)
        entityManager.flush()
        
        when: "finding by email"
        def found = repository.findByEmail("unique@example.com")
        
        then: "customer is found"
        found.isPresent()
        found.get().email == "unique@example.com"
    }

    def "should return empty when customer not found"() {
        when: "finding non-existent customer"
        def bySSN = repository.findBySsn("999-99-9999")
        def byEmail = repository.findByEmail("nonexistent@example.com")
        
        then: "empty optionals are returned"
        !bySSN.isPresent()
        !byEmail.isPresent()
    }

    def "should check existence by SSN"() {
        given: "a customer"
        def customer = createCustomer("333-33-3333", "test3@example.com")
        repository.save(customer)
        entityManager.flush()
        
        when: "checking existence"
        def exists = repository.existsBySsn("333-33-3333")
        def notExists = repository.existsBySsn("999-99-9999")
        
        then: "correct existence results"
        exists == true
        notExists == false
    }

    def "should check existence by email"() {
        given: "a customer"
        def customer = createCustomer("444-44-4444", "exists@example.com")
        repository.save(customer)
        entityManager.flush()
        
        when: "checking existence"
        def exists = repository.existsByEmail("exists@example.com")
        def notExists = repository.existsByEmail("notexists@example.com")
        
        then: "correct existence results"
        exists == true
        notExists == false
    }

    def "should enforce unique SSN constraint"() {
        given: "a customer with SSN"
        def customer1 = createCustomer("555-55-5555", "user1@example.com")
        repository.save(customer1)
        entityManager.flush()
        
        when: "saving another customer with same SSN"
        def customer2 = createCustomer("555-55-5555", "user2@example.com")
        repository.save(customer2)
        
        then: "save succeeds but flush fails"
        noExceptionThrown()
        
        when: "flushing to database"
        entityManager.flush()
        
        then: "constraint violation is thrown"
        def e = thrown(Exception)
        e instanceof DataIntegrityViolationException || e instanceof PersistenceException
    }

    def "should enforce unique email constraint"() {
        given: "a customer with email"
        def customer1 = createCustomer("666-66-6666", "duplicate@example.com")
        repository.save(customer1)
        entityManager.flush()
        
        when: "saving another customer with same email"
        def customer2 = createCustomer("777-77-7777", "duplicate@example.com")
        repository.save(customer2)
        
        then: "save succeeds but flush fails"
        noExceptionThrown()
        
        when: "flushing to database"
        entityManager.flush()
        
        then: "constraint violation is thrown"
        def e = thrown(Exception)
        e instanceof DataIntegrityViolationException || e instanceof PersistenceException
    }

    def "should update customer"() {
        given: "a saved customer"
        def customer = createCustomer()
        def saved = repository.save(customer)
        entityManager.flush()
        entityManager.clear()
        
        when: "updating customer"
        def toUpdate = repository.findById(saved.id).get()
        toUpdate.phoneNumber = "+9876543210"
        toUpdate.address.city = "Updated City"
        repository.save(toUpdate)
        entityManager.flush()
        entityManager.clear()
        
        then: "customer is updated"
        def updated = repository.findById(saved.id).get()
        updated.phoneNumber == "+9876543210"
        updated.address.city == "Updated City"
        updated.updatedAt != null
    }

    def "should delete customer"() {
        given: "a customer"
        def customer = createCustomer()
        def saved = repository.save(customer)
        entityManager.flush()
        
        when: "deleting the customer"
        repository.deleteById(saved.id)
        entityManager.flush()
        
        then: "customer is deleted"
        !repository.findById(saved.id).isPresent()
    }

    def "should find all customers"() {
        given: "multiple customers"
        def customers = [
                createCustomer("111-11-1111", "cust1@example.com"),
                createCustomer("222-22-2222", "cust2@example.com"),
                createCustomer("333-33-3333", "cust3@example.com")
        ]
        repository.saveAll(customers)
        entityManager.flush()
        
        when: "finding all customers"
        def all = repository.findAll()
        
        then: "all customers are returned"
        all.size() >= 3
    }

    def "should handle null address"() {
        given: "a customer without address"
        def customer = createCustomer()
        customer.address = null
        
        when: "saving customer"
        def saved = repository.save(customer)
        entityManager.flush()
        entityManager.clear()
        
        then: "customer is saved without address"
        def found = repository.findById(saved.id).get()
        found.address == null
    }

    // Helper methods
    private Customer createCustomer(String ssn = "123-45-6789", String email = "test@example.com") {
        new Customer(
                firstName: "Test",
                lastName: "Customer",
                email: email,
                ssn: ssn,
                phoneNumber: "+1234567890",
                dateOfBirth: LocalDate.of(1990, 1, 1),
                address: new Address(
                        streetAddress: "123 Test St",
                        city: "Test City",
                        state: "TS",
                        zipCode: "12345",
                        country: "USA"
                ),
                identityVerified: true,
                kycStatus: Customer.KycStatus.PENDING
        )
    }
}
