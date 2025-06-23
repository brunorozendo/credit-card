package com.bank.creditcard.repository;

import com.bank.creditcard.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findBySsn(String ssn);

    Optional<Customer> findByEmail(String email);

    boolean existsBySsn(String ssn);

    boolean existsByEmail(String email);
}
