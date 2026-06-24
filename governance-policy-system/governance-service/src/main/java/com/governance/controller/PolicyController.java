package com.governance.controller;

import com.governance.dto.CreatePolicyRequest;
import com.governance.dto.PolicyResponse;
import com.governance.dto.PolicyStatusUpdateRequest;
import com.governance.service.PolicyService;
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
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
@Tag(name = "Policy Management", description = "APIs for managing governance policies")
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping
    @Operation(summary = "Create a new policy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Policy created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<PolicyResponse> createPolicy(@Valid @RequestBody CreatePolicyRequest request) {
        PolicyResponse response = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all policies")
    public ResponseEntity<List<PolicyResponse>> getAllPolicies() {
        return ResponseEntity.ok(policyService.getAllPolicies());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get policy by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policy found"),
            @ApiResponse(responseCode = "404", description = "Policy not found")
    })
    public ResponseEntity<PolicyResponse> getPolicyById(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.getPolicyById(id));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit policy for approval")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policy submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "404", description = "Policy not found")
    })
    public ResponseEntity<PolicyResponse> submitPolicy(
            @PathVariable Long id,
            @Valid @RequestBody PolicyStatusUpdateRequest request) {
        return ResponseEntity.ok(policyService.submitPolicy(id, request));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a policy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policy approved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "404", description = "Policy not found")
    })
    public ResponseEntity<PolicyResponse> approvePolicy(
            @PathVariable Long id,
            @Valid @RequestBody PolicyStatusUpdateRequest request) {
        return ResponseEntity.ok(policyService.approvePolicy(id, request));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a policy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policy rejected successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "404", description = "Policy not found")
    })
    public ResponseEntity<PolicyResponse> rejectPolicy(
            @PathVariable Long id,
            @Valid @RequestBody PolicyStatusUpdateRequest request) {
        return ResponseEntity.ok(policyService.rejectPolicy(id, request));
    }
}