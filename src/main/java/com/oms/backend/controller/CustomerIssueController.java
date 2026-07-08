package com.oms.backend.controller;

import com.oms.backend.dto.IssueDtos.*;
import com.oms.backend.entity.CustomerIssue;
import com.oms.backend.entity.IssueReply;
import com.oms.backend.service.CustomerIssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CustomerIssueController {

    private final CustomerIssueService issueService;

    // ---- Customer ----
    @PostMapping("/api/customer/issues")
    public ResponseEntity<CustomerIssue> create(Authentication auth, @Valid @RequestBody CreateIssueRequest request) {
        return ResponseEntity.ok(issueService.create(auth.getName(), request));
    }

    @GetMapping("/api/customer/issues")
    public ResponseEntity<List<CustomerIssue>> myIssues(Authentication auth) {
        return ResponseEntity.ok(issueService.getByCustomer(auth.getName()));
    }

    @GetMapping("/api/customer/issues/{issueId}/replies")
    public ResponseEntity<List<IssueReply>> customerReplies(@PathVariable Long issueId) {
        return ResponseEntity.ok(issueService.getReplies(issueId));
    }

    // ---- Admin ----
    @GetMapping("/api/admin/issues")
    public ResponseEntity<List<CustomerIssue>> allIssues() {
        return ResponseEntity.ok(issueService.getAll());
    }

    @PostMapping("/api/admin/issues/{issueId}/reply")
    public ResponseEntity<IssueReply> reply(Authentication auth, @PathVariable Long issueId, @Valid @RequestBody ReplyRequest request) {
        return ResponseEntity.ok(issueService.reply(issueId, auth.getName(), request));
    }

    @PutMapping("/api/admin/issues/{issueId}/status")
    public ResponseEntity<CustomerIssue> updateStatus(@PathVariable Long issueId, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(issueService.updateStatus(issueId, body.get("status")));
    }
}
