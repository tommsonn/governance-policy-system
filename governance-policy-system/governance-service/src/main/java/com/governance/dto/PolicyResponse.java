package com.governance.dto;

import com.governance.model.PolicyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResponse {
    private Long id;
    private String title;
    private String description;
    private PolicyStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}