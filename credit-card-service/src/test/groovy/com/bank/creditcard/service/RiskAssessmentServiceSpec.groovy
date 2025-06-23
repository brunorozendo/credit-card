package com.bank.creditcard.service

import com.bank.creditcard.dto.CreditBureauReport
import com.bank.creditcard.model.CreditCardApplication
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class RiskAssessmentServiceSpec extends Specification {

    @Subject
    def service = new RiskAssessmentService()

    @Unroll
    def "should calculate risk score correctly for credit score #creditScore"() {
        given: "an application and credit report"
        def application = createApplication(annualIncome)
        def creditReport = createCreditReport(creditScore, monthlyDebt, delinquentAccounts)
        
        when: "calculating risk score"
        def riskScore = service.calculateRiskScore(application, creditReport)
        
        then: "risk score is within expected range"
        riskScore >= expectedMinRisk
        riskScore <= expectedMaxRisk
        
        where:
        creditScore | annualIncome | monthlyDebt | delinquentAccounts | expectedMinRisk | expectedMaxRisk
        850         | 100000       | 1000        | 0                  | 5               | 15
        750         | 80000        | 2000        | 0                  | 10              | 25
        650         | 60000        | 3000        | 1                  | 30              | 50
        550         | 40000        | 2500        | 2                  | 60              | 80
    }

    def "should determine appropriate credit limit based on risk"() {
        given: "an application with specific risk score"
        def application = createApplication(annualIncome)
        application.requestedLimit = requestedLimit
        
        when: "determining approved limit"
        def approvedLimit = service.determineApprovedLimit(application, riskScore)
        
        then: "approved limit is calculated correctly"
        approvedLimit == expectedLimit
        
        where:
        annualIncome | requestedLimit | riskScore | expectedLimit
        100000       | 20000         | 20        | 16000  // 100k * 0.2 * 0.8 = 16k
        80000        | 15000         | 30        | 11000  // 80k * 0.2 * 0.7 = 11.2k -> rounded to 11k
        60000        | 10000         | 50        | 6000   // 60k * 0.2 * 0.5 = 6k
        50000        | 15000         | 40        | 6000   // 50k * 0.2 * 0.6 = 6k (less than requested)
    }

    def "should handle zero annual income"() {
        given: "an application with zero income"
        def application = createApplication(0)
        def creditReport = createCreditReport(700, 0, 0)
        
        when: "calculating risk score"
        def riskScore = service.calculateRiskScore(application, creditReport)
        
        then: "risk score reflects high DTI risk"
        riskScore >= 50 // High risk due to zero income
    }

    // Helper methods
    private CreditCardApplication createApplication(BigDecimal annualIncome) {
        new CreditCardApplication(
                annualIncome: annualIncome,
                requestedLimit: BigDecimal.valueOf(10000)
        )
    }

    private CreditBureauReport createCreditReport(int creditScore, BigDecimal monthlyDebt, int delinquentAccounts) {
        def accounts = []
        if (monthlyDebt > 0) {
            accounts << CreditBureauReport.CreditAccount.builder()
                    .accountType("Credit Card")
                    .balance(monthlyDebt.multiply(BigDecimal.valueOf(10)))
                    .creditLimit(monthlyDebt.multiply(BigDecimal.valueOf(20)))
                    .monthlyPayment(monthlyDebt)
                    .build()
        }
        
        CreditBureauReport.builder()
                .creditScore(creditScore)
                .monthlyDebtPayments(monthlyDebt)
                .numberOfDelinquentAccounts(delinquentAccounts)
                .creditAccounts(accounts)
                .recentInquiries([])
                .build()
    }
}
