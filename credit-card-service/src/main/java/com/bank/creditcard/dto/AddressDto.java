package com.bank.creditcard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddressDto {

    @NotBlank(message = "Street address is required")
    private String streetAddress;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "State must be 2-letter code")
    private String state;

    @NotBlank(message = "Zip code is required")
    @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Valid US zip code required")
    private String zipCode;

    @NotBlank(message = "Country is required")
    private String country = "USA";
}
