package com.governance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.governance.dto.GovernanceEvent;
import com.governance.model.OutboxEvent;
import com.governance.model.OutboxStatus;
import com.governance.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, GovernanceEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "governance-events";
    private static final int MAX_RETRIES = 10;
    private static final int BATCH_SIZE = 50;

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents =
                outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending outbox events to publish", pendingEvents.size());
        List<OutboxEvent> batch = pendingEvents.size() > BATCH_SIZE
                ? pendingEvents.subList(0, BATCH_SIZE)
                : pendingEvents;

        int published = 0;
        int failed = 0;

        for (OutboxEvent event : batch) {
            if (publishEvent(event)) {
                published++;
            } else {
                failed++;
            }
        }

        log.info("Outbox publish completed: {} published, {} failed", published, failed);
    }

    @Transactional
    protected boolean publishEvent(OutboxEvent event) {
        try {
            GovernanceEvent payload = objectMapper.readValue(
                    event.getPayload(), GovernanceEvent.class);
            kafkaTemplate.send(TOPIC, payload)
                    .get(10, TimeUnit.SECONDS);

            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(LocalDateTime.now());
            outboxRepository.save(event);

            log.info("✅ Published outbox event: id={}, type={}",
                    event.getId(), event.getEventType());
            return true;

        } catch (Exception e) {
            log.error("❌ Failed to publish outbox event id={}: {}",
                    event.getId(), e.getMessage());

            event.setRetryCount(event.getRetryCount() + 1);
            event.setLastError(e.getMessage());

            if (event.getRetryCount() >= MAX_RETRIES) {
                event.setStatus(OutboxStatus.FAILED);
                log.error("❌ Outbox event id={} exceeded {} retries, marked FAILED",
                        event.getId(), MAX_RETRIES);
            }

            outboxRepository.save(event);
            return false;
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupFailedEvents() {
        log.info(" Running outbox cleanup job");

        List<OutboxEvent> oldFailedEvents = outboxRepository
                .findByStatusAndRetryCountGreaterThanEqual(OutboxStatus.FAILED, MAX_RETRIES);

        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        oldFailedEvents.removeIf(e -> e.getCreatedAt().isAfter(cutoff));

        if (!oldFailedEvents.isEmpty()) {
            outboxRepository.deleteAll(oldFailedEvents);
            log.info(" Deleted {} old failed events", oldFailedEvents.size());
        }
    }
}