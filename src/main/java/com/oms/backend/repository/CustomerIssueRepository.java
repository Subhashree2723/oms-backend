package com.oms.backend.repository;

import com.oms.backend.entity.CustomerIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerIssueRepository extends JpaRepository<CustomerIssue, Long> {
    List<CustomerIssue> findByCustomerId(Long customerId);
    List<CustomerIssue> findByStatus(CustomerIssue.IssueStatus status);
}
