package com.bank.creditcard.integration

import com.bank.creditcard.CreditCardApplication
import com.bank.creditcard.dto.AddressDto
import com.bank.creditcard.dto.CreditCardApplicationRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDate

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = CreditCardApplication)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class CreditCardApplicationIntegrationSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    @Shared
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("creditcard_test")
            .withUsername("test")
            .withPassword("test")

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl)
        registry.add("spring.datasource.username", postgres::getUsername)
        registry.add("spring.datasource.password", postgres::getPassword)
    }

    def "should submit credit card application successfully"() {
        given: "a valid credit card application request"
        def request = createValidRequest()
        def requestJson = objectMapper.writeValueAsString(request)

        when: "submitting the application"
        def result = mockMvc.perform(post("/api/v1/credit-card-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))

        then: "application is created successfully"
        result.andExpect(status().isCreated())
                .andExpect(jsonPath('$.applicationNumber').exists())
                .andExpect(jsonPath('$.status').value("PENDING"))
                .andExpect(jsonPath('$.customerName').value("John Doe"))
                .andExpect(jsonPath('$.email').value("john.doe@example.com"))
                .andExpect(jsonPath('$.requestedLimit').value(5000))
    }

    def "should retrieve application by application number"() {
        given: "an existing application"
        def request = createValidRequest()
        request.ssn = "987-65-4321" // Different SSN to avoid conflict
        def requestJson = objectMapper.writeValueAsString(request)
        
        def createResponse = mockMvc.perform(post("/api/v1/credit-card-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andReturn()
                .response
                .contentAsString
        
        def applicationNumber = objectMapper.readTree(createResponse).get("applicationNumber").asText()

        when: "retrieving the application"
        def result = mockMvc.perform(get("/api/v1/credit-card-applications/{applicationNumber}", applicationNumber))

        then: "application details are returned"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.applicationNumber').value(applicationNumber))
                .andExpect(jsonPath('$.customerName').value("John Doe"))
    }

    def "should return 400 for invalid request"() {
        given: "an invalid request with missing required fields"
        def request = new CreditCardApplicationRequest()
        request.firstName = "John" // Only partial data
        def requestJson = objectMapper.writeValueAsString(request)

        when: "submitting the invalid application"
        def result = mockMvc.perform(post("/api/v1/credit-card-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))

        then: "validation error is returned"
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.title').value("Validation Error"))
                .andExpect(jsonPath('$.errors').exists())
    }

    def "should return 409 for duplicate pending application"() {
        given: "an existing pending application"
        def request = createValidRequest()
        request.ssn = "555-55-5555"
        def requestJson = objectMapper.writeValueAsString(request)
        
        // First application
        mockMvc.perform(post("/api/v1/credit-card-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))

        when: "submitting duplicate application with same SSN"
        def result = mockMvc.perform(post("/api/v1/credit-card-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))

        then: "conflict error is returned"
        result.andExpect(status().isConflict())
                .andExpect(jsonPath('$.title').value("Duplicate Application"))
    }

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
}
