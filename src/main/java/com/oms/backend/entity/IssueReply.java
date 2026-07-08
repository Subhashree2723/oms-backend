package com.oms.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "issue_reply")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IssueReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private CustomerIssue issue;

    @Column(name = "replied_by", nullable = false, length = 50)
    private String repliedBy;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
