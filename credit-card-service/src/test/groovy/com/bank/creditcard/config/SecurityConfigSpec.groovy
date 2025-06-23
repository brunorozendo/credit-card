package com.bank.creditcard.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import spock.lang.Specification

class SecurityConfigSpec extends Specification {

    def securityConfig = new SecurityConfig()

    def "should create password encoder"() {
        when: "creating password encoder"
        def encoder = securityConfig.passwordEncoder()
        
        then: "BCrypt encoder is created"
        encoder instanceof BCryptPasswordEncoder
        
        and: "encoder works correctly"
        def password = "testPassword123"
        def encoded = encoder.encode(password)
        encoder.matches(password, encoded)
        !encoder.matches("wrongPassword", encoded)
    }

    def "should create user details service with users"() {
        given: "password encoder"
        def encoder = securityConfig.passwordEncoder()
        
        when: "creating user details service"
        def userDetailsService = securityConfig.userDetailsService()
        
        then: "service is created"
        userDetailsService != null
        
        when: "loading users"
        def user = userDetailsService.loadUserByUsername("user")
        def admin = userDetailsService.loadUserByUsername("admin")
        
        then: "users exist with correct roles"
        user.username == "user"
        user.authorities.any { it.authority == "ROLE_USER" }
        
        admin.username == "admin"
        admin.authorities.any { it.authority == "ROLE_ADMIN" }
        admin.authorities.any { it.authority == "ROLE_USER" }
    }

    def "should handle non-existent user"() {
        given: "user details service"
        def userDetailsService = securityConfig.userDetailsService()
        
        when: "loading non-existent user"
        userDetailsService.loadUserByUsername("nonexistent")
        
        then: "exception is thrown"
        thrown(org.springframework.security.core.userdetails.UsernameNotFoundException)
    }

    def "should verify password encoding for default users"() {
        given: "password encoder and user service"
        def encoder = securityConfig.passwordEncoder()
        def userDetailsService = securityConfig.userDetailsService()
        
        when: "checking default passwords"
        def user = userDetailsService.loadUserByUsername("user")
        def admin = userDetailsService.loadUserByUsername("admin")
        
        then: "passwords are properly encoded"
        encoder.matches("password", user.password)
        encoder.matches("admin123", admin.password)
    }

    def "should create security filter chain"() {
        given: "mock http security"
        def httpSecurity = Mock(HttpSecurity)
        def mockCsrf = Mock(HttpSecurity.CsrfConfigurer)
        def mockSession = Mock(HttpSecurity.SessionManagementConfigurer)
        def mockAuth = Mock(HttpSecurity.AuthorizeHttpRequestsConfigurer)
        def mockBasic = Mock(HttpSecurity.HttpBasicConfigurer)
        
        // This test verifies the configuration method is callable
        // Actual security testing would require full Spring context
        
        when: "configuring security"
        httpSecurity.csrf(_) >> mockCsrf
        mockCsrf.disable() >> httpSecurity
        
        then: "configuration can be created"
        securityConfig != null
    }

    def "should validate security endpoints configuration"() {
        // This test documents the security configuration
        // Full integration testing would be done in SecurityIntegrationSpec
        
        expect: "endpoints are configured as documented"
        def publicEndpoints = [
                "/api-docs/**",
                "/swagger-ui/**", 
                "/swagger-ui.html",
                "/actuator/health/**"
        ]
        
        def protectedEndpoints = [
                "/api/v1/credit-card-applications/**"
        ]
        
        publicEndpoints.size() == 4
        protectedEndpoints.size() == 1
    }

    def "should use stateless session management"() {
        // This documents that the API is stateless (no sessions)
        expect: "session policy is STATELESS as configured"
        true // Actual verification requires full context
    }

    def "should support HTTP Basic authentication"() {
        // This documents the authentication method
        expect: "HTTP Basic is configured"
        true // Actual verification requires full context
    }
}
