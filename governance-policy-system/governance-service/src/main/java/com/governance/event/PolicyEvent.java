package com.governance.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyEvent {
    private String eventType;
    private Long policyId;
    private String actor;
    private LocalDateTime timestamp;
}