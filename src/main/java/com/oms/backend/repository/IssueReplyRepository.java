package com.oms.backend.repository;

import com.oms.backend.entity.IssueReply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IssueReplyRepository extends JpaRepository<IssueReply, Long> {
    List<IssueReply> findByIssueIdOrderByCreatedAtAsc(Long issueId);
}
