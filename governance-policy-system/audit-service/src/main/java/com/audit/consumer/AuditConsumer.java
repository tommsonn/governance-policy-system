package com.audit.consumer;

import com.audit.entity.AuditLog;
import com.audit.repository.AuditRepository;
import com.governance.dto.GovernanceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditConsumer {

    private final AuditRepository auditRepository;

    @KafkaListener(topics = "governance-events", groupId = "audit-service-group")
    public void consume(GovernanceEvent event) {
        log.info("Received audit event: {}", event);

        AuditLog auditLog = new AuditLog();
        auditLog.setEventType(event.getEventType());
        auditLog.setPolicyId(event.getPolicyId());
        auditLog.setActor(event.getActor());
        auditLog.setTimestamp(event.getTimestamp());

        auditRepository.save(auditLog);
        log.info("✅ Audit log saved for policy {}", event.getPolicyId());
    }
}