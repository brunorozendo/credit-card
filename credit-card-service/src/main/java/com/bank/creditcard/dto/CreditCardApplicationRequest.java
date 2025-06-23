package com.bank.creditcard.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreditCardApplicationRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Valid phone number is required")
    private String phoneNumber;

    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{4}$", message = "SSN must be in format XXX-XX-XXXX")
    private String ssn;

    @Past(message = "Date of birth must be in the past")
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @Valid
    @NotNull(message = "Address is required")
    private AddressDto address;

    @DecimalMin(value = "0.0", message = "Annual income must be positive")
    @NotNull(message = "Annual income is required")
    private BigDecimal annualIncome;

    @NotBlank(message = "Employment status is required")
    private String employmentStatus;

    @DecimalMin(value = "1000.0", message = "Minimum requested limit is $1000")
    @DecimalMax(value = "100000.0", message = "Maximum requested limit is $100000")
    private BigDecimal requestedLimit;

    @NotNull(message = "Card type is required")
    private String cardType;
}
