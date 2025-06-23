package com.bank.creditcard.repository;

import com.bank.creditcard.model.CreditCardApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditCardApplicationRepository extends JpaRepository<CreditCardApplication, UUID> {

    Optional<CreditCardApplication> findByApplicationNumber(String applicationNumber);

    List<CreditCardApplication> findByStatus(CreditCardApplication.ApplicationStatus status);

    @Query("SELECT a FROM CreditCardApplication a WHERE a.customer.email = :email")
    List<CreditCardApplication> findByCustomerEmail(String email);

    @Query("SELECT a FROM CreditCardApplication a WHERE a.customer.ssn = :ssn")
    List<CreditCardApplication> findByCustomerSsn(String ssn);

    boolean existsByCustomerSsnAndStatus(String ssn, CreditCardApplication.ApplicationStatus status);
}
