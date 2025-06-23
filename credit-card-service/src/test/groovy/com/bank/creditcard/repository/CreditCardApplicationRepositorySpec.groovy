package com.bank.creditcard.repository

import com.bank.creditcard.model.Address
import com.bank.creditcard.model.CreditCardApplication
import com.bank.creditcard.model.Customer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import java.time.LocalDate

@DataJpaTest
@ActiveProfiles("test")
class CreditCardApplicationRepositorySpec extends Specification {

    @Autowired
    TestEntityManager entityManager

    @Autowired
    CreditCardApplicationRepository repository

    @Autowired
    CustomerRepository customerRepository

    def "should save and find application by id"() {
        given: "a credit card application"
        def customer = createAndPersistCustomer()
        def application = createApplication(customer)
        
        when: "saving the application"
        def saved = repository.save(application)
        entityManager.flush()
        entityManager.clear()
        
        then: "application is saved with generated id"
        saved.id != null
        saved.applicationNumber != null
        
        when: "finding by id"
        def found = repository.findById(saved.id)
        
        then: "application is found"
        found.isPresent()
        found.get().id == saved.id
        found.get().customer.id == customer.id
    }

    def "should find application by application number"() {
        given: "an application with specific number"
        def customer = createAndPersistCustomer()
        def application = createApplication(customer)
        repository.save(application)
        entityManager.flush()
        
        when: "finding by application number"
        def found = repository.findByApplicationNumber(application.applicationNumber)
        
        then: "application is found"
        found.isPresent()
        found.get().applicationNumber == application.applicationNumber
    }

    def "should return empty when application number not found"() {
        when: "finding non-existent application"
        def found = repository.findByApplicationNumber("APP-NONEXISTENT")
        
        then: "empty optional is returned"
        !found.isPresent()
    }

    def "should find applications by status"() {
        given: "applications with different statuses"
        // Create customers with unique data to avoid constraint violations
        def ts = System.currentTimeMillis()
        def customer1 = new Customer(
                firstName: "Test1",
                lastName: "User1",
                email: "test1_${ts}@example.com",
                ssn: String.format("111-11-%04d", ts % 10000),
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
                kycStatus: Customer.KycStatus.COMPLETED
        )
        entityManager.persist(customer1)
        
        def customer2 = new Customer(
                firstName: "Test2",
                lastName: "User2",
                email: "test2_${ts}@example.com",
                ssn: String.format("222-22-%04d", (ts + 1) % 10000),
                phoneNumber: "+1234567891",
                dateOfBirth: LocalDate.of(1990, 1, 1),
                address: new Address(
                        streetAddress: "124 Test St",
                        city: "Test City",
                        state: "TS",
                        zipCode: "12345",
                        country: "USA"
                ),
                identityVerified: true,
                kycStatus: Customer.KycStatus.COMPLETED
        )
        entityManager.persist(customer2)
        
        def customer3 = new Customer(
                firstName: "Test3",
                lastName: "User3",
                email: "test3_${ts}@example.com",
                ssn: String.format("333-33-%04d", (ts + 2) % 10000),
                phoneNumber: "+1234567892",
                dateOfBirth: LocalDate.of(1990, 1, 1),
                address: new Address(
                        streetAddress: "125 Test St",
                        city: "Test City",
                        state: "TS",
                        zipCode: "12345",
                        country: "USA"
                ),
                identityVerified: true,
                kycStatus: Customer.KycStatus.COMPLETED
        )
        entityManager.persist(customer3)
        entityManager.flush()
        
        def pendingApp = createApplication(customer1, CreditCardApplication.ApplicationStatus.PENDING)
        def approvedApp = createApplication(customer2, CreditCardApplication.ApplicationStatus.APPROVED)
        def rejectedApp = createApplication(customer3, CreditCardApplication.ApplicationStatus.REJECTED)
        
        repository.saveAll([pendingApp, approvedApp, rejectedApp])
        entityManager.flush()
        
        when: "finding pending applications"
        def pendingApps = repository.findByStatus(CreditCardApplication.ApplicationStatus.PENDING)
        
        then: "only pending applications are returned"
        pendingApps.size() == 1
        pendingApps[0].status == CreditCardApplication.ApplicationStatus.PENDING
        
        when: "finding approved applications"
        def approvedApps = repository.findByStatus(CreditCardApplication.ApplicationStatus.APPROVED)
        
        then: "only approved applications are returned"
        approvedApps.size() == 1
        approvedApps[0].status == CreditCardApplication.ApplicationStatus.APPROVED
    }

    def "should find applications by customer email"() {
        given: "customers with applications"
        def timestamp = System.currentTimeMillis()
        def ssn1 = String.format("123-45-%04d", timestamp % 10000)
        def ssn2 = String.format("987-65-%04d", (timestamp + 1) % 10000)
        def customer1 = createAndPersistCustomer("john.doe${timestamp}@example.com", ssn1)
        def customer2 = createAndPersistCustomer("jane.smith${timestamp}@example.com", ssn2)
        
        def app1 = createApplication(customer1)
        def app2 = createApplication(customer1)
        def app3 = createApplication(customer2)
        
        repository.saveAll([app1, app2, app3])
        entityManager.flush()
        
        when: "finding by customer email"
        def johnsApps = repository.findByCustomerEmail(customer1.email)
        
        then: "correct applications are returned"
        johnsApps.size() == 2
        johnsApps.every { it.customer.email == customer1.email }
    }

    def "should find applications by customer SSN"() {
        given: "customers with applications"
        def customer1 = createAndPersistCustomer("john@example.com", "111-11-1111")
        def customer2 = createAndPersistCustomer("jane@example.com", "222-22-2222")
        
        def app1 = createApplication(customer1)
        def app2 = createApplication(customer2)
        
        repository.saveAll([app1, app2])
        entityManager.flush()
        
        when: "finding by SSN"
        def apps = repository.findByCustomerSsn("111-11-1111")
        
        then: "correct applications are returned"
        apps.size() == 1
        apps[0].customer.ssn == "111-11-1111"
    }

    def "should check existence by customer SSN and status"() {
        given: "an application with specific status"
        def customer = createAndPersistCustomer()
        def application = createApplication(customer, CreditCardApplication.ApplicationStatus.PENDING)
        repository.save(application)
        entityManager.flush()
        
        when: "checking existence"
        def existsPending = repository.existsByCustomerSsnAndStatus(customer.ssn, CreditCardApplication.ApplicationStatus.PENDING)
        def existsApproved = repository.existsByCustomerSsnAndStatus(customer.ssn, CreditCardApplication.ApplicationStatus.APPROVED)
        
        then: "correct existence results"
        existsPending == true
        existsApproved == false
    }

    def "should handle empty results correctly"() {
        when: "querying empty database"
        def byStatus = repository.findByStatus(CreditCardApplication.ApplicationStatus.PENDING)
        def byEmail = repository.findByCustomerEmail("nonexistent@example.com")
        def bySsn = repository.findByCustomerSsn("000-00-0000")
        
        then: "empty lists are returned"
        byStatus.isEmpty()
        byEmail.isEmpty()
        bySsn.isEmpty()
    }

    def "should update application status"() {
        given: "a pending application"
        def customer = createAndPersistCustomer()
        def application = createApplication(customer, CreditCardApplication.ApplicationStatus.PENDING)
        def saved = repository.save(application)
        entityManager.flush()
        entityManager.clear()
        
        when: "updating status"
        def toUpdate = repository.findById(saved.id).get()
        toUpdate.status = CreditCardApplication.ApplicationStatus.APPROVED
        toUpdate.approvedLimit = BigDecimal.valueOf(10000)
        repository.save(toUpdate)
        entityManager.flush()
        entityManager.clear()
        
        then: "status is updated"
        def updated = repository.findById(saved.id).get()
        updated.status == CreditCardApplication.ApplicationStatus.APPROVED
        updated.approvedLimit == BigDecimal.valueOf(10000)
    }

    def "should delete application"() {
        given: "an application"
        def customer = createAndPersistCustomer("delete@example.com", "999-99-9999")
        def application = createApplication(customer)
        def saved = repository.save(application)
        entityManager.flush()
        entityManager.clear()
        
        when: "deleting the application"
        repository.deleteById(saved.id)
        entityManager.flush()
        
        then: "application is deleted"
        !repository.findById(saved.id).isPresent()
        
        and: "customer still exists"
        def customerStillExists = customerRepository.findById(customer.id).isPresent()
        customerStillExists == true
    }

    // Helper methods
    private Customer createAndPersistCustomer(String email = "test@example.com", String ssn = "123-45-6789") {
        def customer = new Customer(
                firstName: "Test",
                lastName: "User",
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
                kycStatus: Customer.KycStatus.COMPLETED
        )
        entityManager.persist(customer)
        entityManager.flush()
        return customer
    }

    private CreditCardApplication createApplication(Customer customer, 
                                                   CreditCardApplication.ApplicationStatus status = CreditCardApplication.ApplicationStatus.PENDING) {
        new CreditCardApplication(
                status: status,
                customer: customer,
                requestedLimit: BigDecimal.valueOf(5000),
                annualIncome: BigDecimal.valueOf(75000),
                employmentStatus: "FULL_TIME",
                cardType: CreditCardApplication.CardType.GOLD
        )
    }
}
