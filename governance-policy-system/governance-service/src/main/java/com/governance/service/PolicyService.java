package com.governance.service;

import com.governance.dto.CreatePolicyRequest;
import com.governance.dto.GovernanceEvent;
import com.governance.dto.PolicyResponse;
import com.governance.dto.PolicyStatusUpdateRequest;
import com.governance.exception.InvalidPolicyStatusTransitionException;
import com.governance.exception.PolicyNotFoundException;
import com.governance.model.Policy;
import com.governance.model.PolicyStatus;
import com.governance.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final KafkaTemplate<String, GovernanceEvent> kafkaTemplate;
    private static final String GOVERNANCE_EVENTS_TOPIC = "governance-events";

    @Transactional
    public PolicyResponse createPolicy(CreatePolicyRequest request) {
        log.info("Creating new policy: {}", request.getTitle());

        Policy policy = Policy.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(PolicyStatus.DRAFT)
                .createdBy(request.getCreatedBy())
                .build();

        Policy savedPolicy = policyRepository.save(policy);

        publishEvent(GovernanceEvent.builder()
                .eventType("policy-created")
                .policyId(savedPolicy.getId())
                .actor(savedPolicy.getCreatedBy())
                .policyTitle(savedPolicy.getTitle())
                .newStatus(savedPolicy.getStatus().toString())
                .timestamp(LocalDateTime.now())
                .build());

        log.info("Policy created successfully with ID: {}", savedPolicy.getId());
        return mapToResponse(savedPolicy);
    }

    public List<PolicyResponse> getAllPolicies() {
        log.info("Fetching all policies");
        return policyRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PolicyResponse getPolicyById(Long id) {
        log.info("Fetching policy with ID: {}", id);
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found with ID: " + id));
        return mapToResponse(policy);
    }

    @Transactional
    public PolicyResponse submitPolicy(Long id, PolicyStatusUpdateRequest request) {
        log.info("Submitting policy with ID: {} by: {}", id, request.getActor());

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found with ID: " + id));

        validateStatusTransition(policy.getStatus(), PolicyStatus.PENDING_APPROVAL);

        policy.setStatus(PolicyStatus.PENDING_APPROVAL);
        Policy updatedPolicy = policyRepository.save(policy);

        publishEvent(GovernanceEvent.builder()
                .eventType("policy-submitted")
                .policyId(updatedPolicy.getId())
                .actor(request.getActor())
                .policyTitle(updatedPolicy.getTitle())
                .previousStatus(PolicyStatus.DRAFT.toString())
                .newStatus(updatedPolicy.getStatus().toString())
                .timestamp(LocalDateTime.now())
                .build());

        log.info("Policy submitted successfully with ID: {}", id);
        return mapToResponse(updatedPolicy);
    }

    @Transactional
    public PolicyResponse approvePolicy(Long id, PolicyStatusUpdateRequest request) {
        log.info("Approving policy with ID: {} by: {}", id, request.getActor());

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found with ID: " + id));

        validateStatusTransition(policy.getStatus(), PolicyStatus.APPROVED);

        policy.setStatus(PolicyStatus.APPROVED);
        Policy updatedPolicy = policyRepository.save(policy);

        publishEvent(GovernanceEvent.builder()
                .eventType("policy-approved")
                .policyId(updatedPolicy.getId())
                .actor(request.getActor())
                .policyTitle(updatedPolicy.getTitle())
                .previousStatus(PolicyStatus.PENDING_APPROVAL.toString())
                .newStatus(updatedPolicy.getStatus().toString())
                .timestamp(LocalDateTime.now())
                .build());

        log.info("Policy approved successfully with ID: {}", id);
        return mapToResponse(updatedPolicy);
    }

    @Transactional
    public PolicyResponse rejectPolicy(Long id, PolicyStatusUpdateRequest request) {
        log.info("Rejecting policy with ID: {} by: {}", id, request.getActor());

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found with ID: " + id));

        validateStatusTransition(policy.getStatus(), PolicyStatus.REJECTED);

        policy.setStatus(PolicyStatus.REJECTED);
        Policy updatedPolicy = policyRepository.save(policy);

        publishEvent(GovernanceEvent.builder()
                .eventType("policy-rejected")
                .policyId(updatedPolicy.getId())
                .actor(request.getActor())
                .policyTitle(updatedPolicy.getTitle())
                .previousStatus(PolicyStatus.PENDING_APPROVAL.toString())
                .newStatus(updatedPolicy.getStatus().toString())
                .timestamp(LocalDateTime.now())
                .build());

        log.info("Policy rejected successfully with ID: {}", id);
        return mapToResponse(updatedPolicy);
    }

    private void validateStatusTransition(PolicyStatus currentStatus, PolicyStatus targetStatus) {
        if (currentStatus == PolicyStatus.DRAFT) {
            if (targetStatus != PolicyStatus.PENDING_APPROVAL) {
                throw new InvalidPolicyStatusTransitionException(
                        "DRAFT policy can only be submitted for approval");
            }
        } else if (currentStatus == PolicyStatus.PENDING_APPROVAL) {
            if (targetStatus != PolicyStatus.APPROVED && targetStatus != PolicyStatus.REJECTED) {
                throw new InvalidPolicyStatusTransitionException(
                        "PENDING_APPROVAL policy can only be approved or rejected");
            }
        } else {
            throw new InvalidPolicyStatusTransitionException(
                    "Cannot change status from " + currentStatus + " to " + targetStatus);
        }
    }

    private void publishEvent(GovernanceEvent event) {
        try {
            kafkaTemplate.send(GOVERNANCE_EVENTS_TOPIC, event);
            log.info("Event published: {}", event.getEventType());
        } catch (Exception e) {
            log.error("Failed to publish event: {}", event.getEventType(), e);
        }
    }

    private PolicyResponse mapToResponse(Policy policy) {
        return PolicyResponse.builder()
                .id(policy.getId())
                .title(policy.getTitle())
                .description(policy.getDescription())
                .status(policy.getStatus())
                .createdBy(policy.getCreatedBy())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }
}