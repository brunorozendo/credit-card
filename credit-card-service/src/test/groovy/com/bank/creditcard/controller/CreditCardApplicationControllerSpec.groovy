package com.bank.creditcard.controller

import com.bank.creditcard.dto.CreditCardApplicationRequest
import com.bank.creditcard.dto.CreditCardApplicationResponse
import com.bank.creditcard.dto.AddressDto
import com.bank.creditcard.exception.DuplicateApplicationException
import com.bank.creditcard.exception.ResourceNotFoundException
import com.bank.creditcard.service.CreditCardApplicationService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDate
import java.time.LocalDateTime

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class CreditCardApplicationControllerSpec extends Specification {

    def applicationService = Mock(CreditCardApplicationService)
    def objectMapper = new ObjectMapper()
    
    @Subject
    def controller = new CreditCardApplicationController(applicationService)
    
    MockMvc mockMvc
    
    def setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build()
        objectMapper.findAndRegisterModules()
    }

    def "should submit application successfully"() {
        given: "a valid application request"
        def request = createValidRequest()
        def response = createResponse("PENDING")
        def requestJson = objectMapper.writeValueAsString(request)
        
        when: "submitting the application"
        def result = mockMvc.perform(post("/api/v1/credit-card-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        
        then: "service is called and response is returned"
        1 * applicationService.submitApplication(request) >> response
        
        and: "correct status and response"
        result.andExpect(status().isCreated())
                .andExpect(jsonPath('$.applicationNumber').value(response.applicationNumber))
                .andExpect(jsonPath('$.status').value("PENDING"))
    }

    def "should handle duplicate application exception"() {
        given: "a request that will cause duplicate exception"
        def request = createValidRequest()
        def requestJson = objectMapper.writeValueAsString(request)
        
        when: "submitting duplicate application"
        def result = mockMvc.perform(post("/api/v1/credit-card-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        
        then: "service throws exception"
        1 * applicationService.submitApplication(request) >> { throw new DuplicateApplicationException("Duplicate application") }
        
        and: "conflict status is returned"
        result.andExpect(status().isConflict())
                .andExpect(jsonPath('$.title').value("Duplicate Application"))
    }

    def "should handle validation errors"() {
        given: "an invalid request"
        def invalidRequest = """
        {
            "firstName": "John"
        }
        """
        
        when: "submitting invalid application"
        def result = mockMvc.perform(post("/api/v1/credit-card-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
        
        then: "bad request status is returned"
        0 * applicationService.submitApplication(_)
        result.andExpect(status().isBadRequest())
    }

    def "should get application by application number"() {
        given: "an application number"
        def applicationNumber = "APP-123456"
        def response = createResponse("APPROVED")
        
        when: "retrieving the application"
        def result = mockMvc.perform(get("/api/v1/credit-card-applications/{applicationNumber}", applicationNumber))
        
        then: "service is called"
        1 * applicationService.getApplication(applicationNumber) >> response
        
        and: "application is returned"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.applicationNumber').value(applicationNumber))
    }

    def "should handle application not found"() {
        given: "a non-existent application number"
        def applicationNumber = "APP-INVALID"
        
        when: "retrieving the application"
        def result = mockMvc.perform(get("/api/v1/credit-card-applications/{applicationNumber}", applicationNumber))
        
        then: "service throws exception"
        1 * applicationService.getApplication(applicationNumber) >> { throw new ResourceNotFoundException("Application not found") }
        
        and: "not found status is returned"
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath('$.title').value("Resource Not Found"))
    }

    def "should get applications by email"() {
        given: "a customer email"
        def email = "john.doe@example.com"
        def applications = [createResponse("APPROVED"), createResponse("REJECTED")]
        
        when: "retrieving applications by email"
        def result = mockMvc.perform(get("/api/v1/credit-card-applications/customer/{email}", email))
        
        then: "service returns applications"
        1 * applicationService.getApplicationsByEmail(email) >> applications
        
        and: "applications are returned"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$').isArray())
                .andExpect(jsonPath('$.length()').value(2))
    }

    def "should get applications by email failed"() {
        given: "a customer email"
        def email = "john.doe@example.com"
        def applications = [createResponse("APPROVED"), createResponse("REJECTED")]

        when: "retrieving applications by email"
        def result = mockMvc.perform(get("/api/v1/credit-card-applications/customer/{email}", email))

        then: "service returns applications"
        1 * applicationService.getApplicationsByEmail(email) >> applications

        and: "applications are returned"
        result.andExpect(status().is(400))
        result.andExpect(jsonPath('$').isArray())
        result.andExpect(jsonPath('$.length()').value(2))
    }

    def "should get pending applications"() {
        given: "pending applications exist"
        def applications = [createResponse("PENDING"), createResponse("PENDING")]
        
        when: "retrieving pending applications"
        def result = mockMvc.perform(get("/api/v1/credit-card-applications/pending"))
        
        then: "service returns pending applications"
        1 * applicationService.getPendingApplications() >> applications
        
        and: "applications are returned"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$').isArray())
                .andExpect(jsonPath('$.length()').value(2))
    }

    def "should return empty list when no applications found"() {
        given: "no applications exist"
        def email = "nonexistent@example.com"
        
        when: "retrieving applications"
        def result = mockMvc.perform(get("/api/v1/credit-card-applications/customer/{email}", email))
        
        then: "empty list is returned"
        1 * applicationService.getApplicationsByEmail(email) >> []
        
        and: "empty array response"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$').isArray())
                .andExpect(jsonPath('$.length()').value(0))
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
                address: createAddressDto(),
                annualIncome: BigDecimal.valueOf(75000),
                employmentStatus: "FULL_TIME",
                requestedLimit: BigDecimal.valueOf(5000),
                cardType: "GOLD"
        )
    }

    private createAddressDto() {
        new com.bank.creditcard.dto.AddressDto(
                streetAddress: "123 Main St",
                city: "New York",
                state: "NY",
                zipCode: "10001",
                country: "USA"
        )
    }

    private CreditCardApplicationResponse createResponse(String status) {
        CreditCardApplicationResponse.builder()
                .id(UUID.randomUUID())
                .applicationNumber("APP-123456")
                .status(status)
                .customerName("John Doe")
                .email("john.doe@example.com")
                .requestedLimit(BigDecimal.valueOf(5000))
                .approvedLimit(status == "APPROVED" ? BigDecimal.valueOf(10000) : null)
                .cardType("GOLD")
                .creditScore(750)
                .riskScore(BigDecimal.valueOf(25))
                .decisionReason(status == "APPROVED" ? "Approved" : "Rejected")
                .createdAt(LocalDateTime.now())
                .decidedAt(status != "PENDING" ? LocalDateTime.now() : null)
                .build()
    }
}
