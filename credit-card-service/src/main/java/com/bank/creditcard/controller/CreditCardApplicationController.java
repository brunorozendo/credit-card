package com.bank.creditcard.controller;

import com.bank.creditcard.dto.CreditCardApplicationRequest;
import com.bank.creditcard.dto.CreditCardApplicationResponse;
import com.bank.creditcard.service.CreditCardApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/credit-card-applications")
@RequiredArgsConstructor
@Tag(name = "Credit Card Applications", description = "APIs for managing credit card applications")
public class CreditCardApplicationController {

    private final CreditCardApplicationService applicationService;

    @PostMapping
    @Operation(summary = "Submit a new credit card application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Application submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Duplicate application")
    })
    public ResponseEntity<CreditCardApplicationResponse> submitApplication(
            @Valid @RequestBody CreditCardApplicationRequest request) {
        CreditCardApplicationResponse response = applicationService.submitApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{applicationNumber}")
    @Operation(summary = "Get application by application number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application found"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    public ResponseEntity<CreditCardApplicationResponse> getApplication(
            @PathVariable String applicationNumber) {
        CreditCardApplicationResponse response = applicationService.getApplication(applicationNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{email}")
    @Operation(summary = "Get applications by customer email")
    public ResponseEntity<List<CreditCardApplicationResponse>> getApplicationsByEmail(
            @PathVariable String email) {
        List<CreditCardApplicationResponse> applications = applicationService.getApplicationsByEmail(email);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get all pending applications")
    public ResponseEntity<List<CreditCardApplicationResponse>> getPendingApplications() {
        List<CreditCardApplicationResponse> applications = applicationService.getPendingApplications();
        return ResponseEntity.ok(applications);
    }
}
