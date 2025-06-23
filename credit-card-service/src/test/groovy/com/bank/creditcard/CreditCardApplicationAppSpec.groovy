package com.bank.creditcard

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@SpringBootTest
@ActiveProfiles("test")
class CreditCardApplicationAppSpec extends Specification {

    def "should load application context"() {
        expect: "application context loads successfully"
        true // Context loads if we get here
    }

    def "should run main method"() {
        when: "running main method with test profile"
        CreditCardApplication.main(["--spring.profiles.active=test"] as String[])
        
        then: "application starts without exception"
        noExceptionThrown()
    }
}
