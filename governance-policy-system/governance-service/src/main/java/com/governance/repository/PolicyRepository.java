package com.governance.repository;

import com.governance.model.Policy;
import com.governance.model.PolicyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    List<Policy> findByStatus(PolicyStatus status);
    List<Policy> findByCreatedBy(String createdBy);
}