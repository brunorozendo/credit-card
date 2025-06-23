package com.bank.creditcard.client

import com.bank.creditcard.dto.CreditBureauReport
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDate

class CreditBureauClientSpec extends Specification {

    @Subject
    def client = new CreditBureauClient()

    def "should get credit report for SSN"() {
        given: "a valid SSN"
        def ssn = "123-45-6789"
        
        when: "fetching credit report"
        def report = client.getCreditReport(ssn)
        
        then: "report is generated with all required fields"
        report != null
        report.ssn == ssn
        report.creditScore >= 300
        report.creditScore <= 850
        report.totalDebt != null
        report.monthlyDebtPayments != null
        report.numberOfAccounts > 0
        report.numberOfDelinquentAccounts >= 0
        report.creditAccounts != null
        report.recentInquiries != null
        report.reportDate != null
    }

    def "should generate credit score with proper distribution"() {
        given: "multiple SSNs to test distribution"
        def ssns = (1..100).collect { "123-45-${String.format('%04d', it)}" }
        
        when: "fetching multiple credit reports"
        def scores = ssns.collect { ssn ->
            client.getCreditReport(ssn).creditScore
        }
        
        then: "scores follow expected distribution"
        def goodScores = scores.findAll { it >= 650 }.size()
        def mediumScores = scores.findAll { it >= 580 && it < 650 }.size()
        def poorScores = scores.findAll { it < 580 }.size()
        
        // Allow for some variance in random distribution
        goodScores >= 50  // Expecting ~70%
        mediumScores >= 10 // Expecting ~20%
        poorScores >= 5    // Expecting ~10%
    }

    def "should generate valid credit accounts"() {
        given: "a valid SSN"
        def ssn = "123-45-6789"
        
        when: "fetching credit report"
        def report = client.getCreditReport(ssn)
        
        then: "credit accounts are valid"
        report.creditAccounts.each { account ->
            assert account.accountType in ["Credit Card", "Auto Loan", "Mortgage", "Personal Loan", "Student Loan"]
            assert account.creditorName in ["Bank of America", "Chase", "Wells Fargo", "Capital One", "Discover"]
            assert account.balance >= BigDecimal.ZERO
            assert account.creditLimit > BigDecimal.ZERO
            assert account.balance <= account.creditLimit
            assert account.monthlyPayment != null
            assert account.status in ["Current", "30 Days Late", "60 Days Late"]
            assert account.openDate != null
            assert account.openDate.isBefore(LocalDate.now()) || account.openDate.isEqual(LocalDate.now())
        }
    }

    def "should calculate total debt correctly"() {
        given: "a credit report"
        def report = client.getCreditReport("123-45-6789")
        
        when: "calculating total debt manually"
        def calculatedTotal = report.creditAccounts
                .collect { it.balance }
                .inject(BigDecimal.ZERO) { sum, balance -> sum + balance }
        
        then: "total debt matches"
        report.totalDebt == calculatedTotal
    }

    def "should calculate monthly payments correctly"() {
        given: "a credit report"
        def report = client.getCreditReport("123-45-6789")
        
        when: "calculating monthly payments manually"
        def calculatedPayments = report.creditAccounts
                .collect { it.monthlyPayment }
                .inject(BigDecimal.ZERO) { sum, payment -> sum + payment }
        
        then: "monthly payments match"
        report.monthlyDebtPayments == calculatedPayments
    }

    def "should generate recent inquiries within 90 days"() {
        given: "a credit report"
        def report = client.getCreditReport("123-45-6789")
        
        when: "checking inquiry dates"
        def today = LocalDate.now()
        
        then: "all inquiries are within 90 days"
        report.recentInquiries.each { inquiry ->
            assert inquiry.inquirerName in ["Target", "Best Buy", "Amazon Store Card", "Home Depot"]
            assert inquiry.inquiryType == "Hard Inquiry"
            assert inquiry.inquiryDate != null
            assert inquiry.inquiryDate.isAfter(today.minusDays(90))
            assert inquiry.inquiryDate.isBefore(today.plusDays(1))
        }
    }

    def "should have reasonable balance to limit ratios"() {
        given: "a credit report"
        def report = client.getCreditReport("123-45-6789")
        
        when: "checking credit card utilization"
        def creditCards = report.creditAccounts.findAll { it.accountType == "Credit Card" }
        
        then: "utilization is reasonable (0-50%)"
        creditCards.each { card ->
            def utilization = card.balance / card.creditLimit
            assert utilization >= 0
            assert utilization <= 0.5
        }
    }

    def "should handle multiple calls efficiently"() {
        given: "the same SSN"
        def ssn = "123-45-6789"
        
        when: "calling multiple times"
        def startTime = System.currentTimeMillis()
        def report1 = client.getCreditReport(ssn)
        def report2 = client.getCreditReport(ssn)
        def endTime = System.currentTimeMillis()
        
        then: "reports are generated independently"
        report1 != null
        report2 != null
        // Reports might be different due to randomization
        report1.ssn == report2.ssn
        
        and: "execution time is reasonable (with simulated delay)"
        endTime - startTime >= 1000 // At least 1 second (2 calls with 500-1000ms delay each)
        endTime - startTime < 3000  // Less than 3 seconds
    }
}
