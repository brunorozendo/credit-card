package com.bank.creditcard.mapper;

import com.bank.creditcard.dto.AddressDto;
import com.bank.creditcard.dto.CreditCardApplicationRequest;
import com.bank.creditcard.dto.CreditCardApplicationResponse;
import com.bank.creditcard.model.Address;
import com.bank.creditcard.model.CreditCardApplication;
import com.bank.creditcard.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "applicationNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "approvedLimit", ignore = true)
    @Mapping(target = "creditScore", ignore = true)
    @Mapping(target = "riskScore", ignore = true)
    @Mapping(target = "decisionReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "decidedAt", ignore = true)
    @Mapping(source = "cardType", target = "cardType")
    CreditCardApplication toEntity(CreditCardApplicationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "identityVerified", ignore = true)
    @Mapping(target = "kycStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toCustomer(CreditCardApplicationRequest request);

    Address toAddress(AddressDto addressDto);

    @Mapping(target = "customerName", expression = "java(application.getCustomer().getFirstName() + \" \" + application.getCustomer().getLastName())")
    @Mapping(source = "customer.email", target = "email")
    CreditCardApplicationResponse toResponse(CreditCardApplication application);
}
