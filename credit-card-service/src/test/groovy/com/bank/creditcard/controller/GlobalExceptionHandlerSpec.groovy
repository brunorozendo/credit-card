package com.bank.creditcard.controller

import com.bank.creditcard.exception.DuplicateApplicationException
import com.bank.creditcard.exception.ResourceNotFoundException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class GlobalExceptionHandlerSpec extends Specification {

    def exceptionHandler = new GlobalExceptionHandler()
    def testController = new TestController()
    def objectMapper = new ObjectMapper()
    
    MockMvc mockMvc
    
    def setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(testController)
                .setControllerAdvice(exceptionHandler)
                .build()
        objectMapper.findAndRegisterModules()
    }

    def "should handle ResourceNotFoundException"() {
        when: "endpoint throws ResourceNotFoundException"
        def result = mockMvc.perform(get("/test/resource-not-found"))
        
        then: "404 status with proper error details"
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath('$.title').value("Resource Not Found"))
                .andExpect(jsonPath('$.detail').value("Test resource not found"))
                .andExpect(jsonPath('$.status').value(404))
                .andExpect(jsonPath('$.timestamp').exists())
    }

    def "should handle DuplicateApplicationException"() {
        when: "endpoint throws DuplicateApplicationException"
        def result = mockMvc.perform(get("/test/duplicate"))
        
        then: "409 status with proper error details"
        result.andExpect(status().isConflict())
                .andExpect(jsonPath('$.title').value("Duplicate Application"))
                .andExpect(jsonPath('$.detail').value("Duplicate test"))
                .andExpect(jsonPath('$.status').value(409))
                .andExpect(jsonPath('$.timestamp').exists())
    }

    def "should handle MethodArgumentNotValidException"() {
        when: "endpoint throws validation exception"
        def result = mockMvc.perform(get("/test/validation"))
        
        then: "400 status with validation errors"
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.title').value("Validation Error"))
                .andExpect(jsonPath('$.detail').value("Validation failed"))
                .andExpect(jsonPath('$.status').value(400))
                .andExpect(jsonPath('$.errors').exists())
                .andExpect(jsonPath('$.errors.firstName').value("First name is required"))
                .andExpect(jsonPath('$.errors.email').value("Invalid email"))
    }

    def "should handle generic Exception"() {
        when: "endpoint throws generic exception"
        def result = mockMvc.perform(get("/test/generic-error"))
        
        then: "500 status with generic error message"
        result.andExpect(status().isInternalServerError())
                .andExpect(jsonPath('$.title').value("Internal Server Error"))
                .andExpect(jsonPath('$.detail').value("An unexpected error occurred"))
                .andExpect(jsonPath('$.status').value(500))
                .andExpect(jsonPath('$.timestamp').exists())
    }

    def "should handle RuntimeException"() {
        when: "endpoint throws runtime exception"
        def result = mockMvc.perform(get("/test/runtime-error"))
        
        then: "500 status is returned"
        result.andExpect(status().isInternalServerError())
                .andExpect(jsonPath('$.title').value("Internal Server Error"))
    }

    // Test controller for exception testing
    @org.springframework.web.bind.annotation.RestController
    @org.springframework.web.bind.annotation.RequestMapping("/test")
    static class TestController {
        
        @org.springframework.web.bind.annotation.GetMapping("/resource-not-found")
        void resourceNotFound() {
            throw new ResourceNotFoundException("Test resource not found")
        }
        
        @org.springframework.web.bind.annotation.GetMapping("/duplicate")
        void duplicate() {
            throw new DuplicateApplicationException("Duplicate test")
        }
        
        @org.springframework.web.bind.annotation.GetMapping("/validation")
        void validation() {
            def bindingResult = new org.springframework.validation.BeanPropertyBindingResult(new Object(), "test")
            bindingResult.addError(new FieldError("test", "firstName", "First name is required"))
            bindingResult.addError(new FieldError("test", "email", "Invalid email"))
            throw new MethodArgumentNotValidException(null, bindingResult)
        }
        
        @org.springframework.web.bind.annotation.GetMapping("/generic-error")
        void genericError() {
            throw new Exception("Generic error")
        }
        
        @org.springframework.web.bind.annotation.GetMapping("/runtime-error")
        void runtimeError() {
            throw new RuntimeException("Runtime error")
        }
    }
}
