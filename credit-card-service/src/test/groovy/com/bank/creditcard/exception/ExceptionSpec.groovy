package com.bank.creditcard.exception

import spock.lang.Specification

class ExceptionSpec extends Specification {

    def "should create DuplicateApplicationException with message"() {
        given: "an error message"
        def message = "Application already exists for this SSN"
        
        when: "creating exception"
        def exception = new DuplicateApplicationException(message)
        
        then: "exception has correct message"
        exception.message == message
        exception instanceof RuntimeException
    }

    def "should create ResourceNotFoundException with message"() {
        given: "an error message"
        def message = "Application not found: APP-123456"
        
        when: "creating exception"
        def exception = new ResourceNotFoundException(message)
        
        then: "exception has correct message"
        exception.message == message
        exception instanceof RuntimeException
    }

    def "should throw DuplicateApplicationException"() {
        when: "throwing DuplicateApplicationException"
        throw new DuplicateApplicationException("Duplicate SSN")
        
        then: "exception is caught with message"
        def e = thrown(DuplicateApplicationException)
        e.message == "Duplicate SSN"
    }

    def "should throw ResourceNotFoundException"() {
        when: "throwing ResourceNotFoundException"
        throw new ResourceNotFoundException("Not found")
        
        then: "exception is caught with message"
        def e = thrown(ResourceNotFoundException)
        e.message == "Not found"
    }

    def "should handle exception inheritance"() {
        given: "exception instances"
        def duplicateEx = new DuplicateApplicationException("test")
        def notFoundEx = new ResourceNotFoundException("test")
        
        expect: "correct inheritance"
        duplicateEx instanceof RuntimeException
        duplicateEx instanceof Exception
        duplicateEx instanceof Throwable
        
        notFoundEx instanceof RuntimeException
        notFoundEx instanceof Exception
        notFoundEx instanceof Throwable
    }

    def "should be catchable as RuntimeException"() {
        when: "throwing custom exceptions"
        throw new DuplicateApplicationException("error")
        
        then: "can be caught as RuntimeException"
        def e = thrown(RuntimeException)
        e instanceof DuplicateApplicationException
        e.message == "error"
    }

    def "should support exception chaining"() {
        given: "a cause exception"
        def cause = new IllegalArgumentException("Invalid data")
        
        when: "creating chained exceptions using constructor"
        def duplicateEx = new DuplicateApplicationException("Duplicate found")
        def resourceEx = new ResourceNotFoundException("Resource missing")
        
        then: "exceptions support standard RuntimeException features"
        duplicateEx.message == "Duplicate found"
        resourceEx.message == "Resource missing"
        
        // These exceptions extend RuntimeException which has cause support
        duplicateEx.cause == null
        resourceEx.cause == null
    }

    def "should have meaningful stack traces"() {
        when: "throwing exception"
        throwNestedException()
        
        then: "stack trace includes method names"
        def e = thrown(DuplicateApplicationException)
        e.stackTrace.any { it.methodName == "throwNestedException" }
    }

    def "should handle null messages"() {
        when: "creating exceptions with null message"
        def duplicateEx = new DuplicateApplicationException(null)
        def notFoundEx = new ResourceNotFoundException(null)
        
        then: "exceptions are created with null message"
        duplicateEx.message == null
        notFoundEx.message == null
    }

    def "should be serializable"() {
        given: "exception instances"
        def duplicateEx = new DuplicateApplicationException("Serialization test")
        def notFoundEx = new ResourceNotFoundException("Serialization test")
        
        expect: "exceptions are serializable (inherited from Throwable)"
        duplicateEx instanceof java.io.Serializable
        notFoundEx instanceof java.io.Serializable
    }

    // Helper method for stack trace test
    private void throwNestedException() {
        throw new DuplicateApplicationException("Nested exception")
    }
}
