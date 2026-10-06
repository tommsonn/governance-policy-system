package com.governance.repository;

import com.governance.model.OutboxEvent;
import com.governance.model.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = :status " +
            "ORDER BY o.createdAt ASC")
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(
            @Param("status") OutboxStatus status);
    List<OutboxEvent> findByStatusAndRetryCountGreaterThanEqual(
            OutboxStatus status, int retryCount);
}