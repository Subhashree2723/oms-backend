package com.oms.backend.service;

import com.oms.backend.dto.IssueDtos.*;
import com.oms.backend.entity.CustomerIssue;
import com.oms.backend.entity.IssueReply;
import java.util.List;

public interface CustomerIssueService {
    CustomerIssue create(String username, CreateIssueRequest request);
    List<CustomerIssue> getByCustomer(String username);
    List<CustomerIssue> getAll();
    IssueReply reply(Long issueId, String repliedBy, ReplyRequest request);
    List<IssueReply> getReplies(Long issueId);
    CustomerIssue updateStatus(Long issueId, String status);
}
