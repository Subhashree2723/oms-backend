package com.oms.backend.serviceimpl;

import com.oms.backend.dto.IssueDtos.*;
import com.oms.backend.entity.Customer;
import com.oms.backend.entity.CustomerIssue;
import com.oms.backend.entity.IssueReply;
import com.oms.backend.exception.BadRequestException;
import com.oms.backend.exception.ResourceNotFoundException;
import com.oms.backend.repository.CustomerIssueRepository;
import com.oms.backend.repository.CustomerRepository;
import com.oms.backend.repository.IssueReplyRepository;
import com.oms.backend.service.CustomerIssueService;
import com.oms.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerIssueServiceImpl implements CustomerIssueService {

    private final CustomerIssueRepository customerIssueRepository;
    private final CustomerRepository customerRepository;
    private final IssueReplyRepository issueReplyRepository;
    private final NotificationService notificationService;

    @Override
    public CustomerIssue create(String username, CreateIssueRequest request) {
        Customer customer = customerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for user: " + username));

        CustomerIssue issue = CustomerIssue.builder()
                .customer(customer)
                .subject(request.getSubject())
                .description(request.getDescription())
                .status(CustomerIssue.IssueStatus.OPEN)
                .build();
        return customerIssueRepository.save(issue);
    }

    @Override
    public List<CustomerIssue> getByCustomer(String username) {
        Customer customer = customerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for user: " + username));
        return customerIssueRepository.findByCustomerId(customer.getId());
    }

    @Override
    public List<CustomerIssue> getAll() {
        return customerIssueRepository.findAll();
    }

    @Override
    public IssueReply reply(Long issueId, String repliedBy, ReplyRequest request) {
        CustomerIssue issue = customerIssueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));

        IssueReply reply = IssueReply.builder()
                .issue(issue)
                .repliedBy(repliedBy)
                .message(request.getMessage())
                .build();
        reply = issueReplyRepository.save(reply);

        if (issue.getStatus() == CustomerIssue.IssueStatus.OPEN) {
            issue.setStatus(CustomerIssue.IssueStatus.IN_PROGRESS);
            customerIssueRepository.save(issue);
        }

        notificationService.notify(issue.getCustomer().getUser().getId(), "Support Reply Received",
                "New reply on your issue: " + issue.getSubject());

        return reply;
    }

    @Override
    public List<IssueReply> getReplies(Long issueId) {
        return issueReplyRepository.findByIssueIdOrderByCreatedAtAsc(issueId);
    }

    @Override
    public CustomerIssue updateStatus(Long issueId, String status) {
        CustomerIssue issue = customerIssueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));
        try {
            issue.setStatus(CustomerIssue.IssueStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid issue status: " + status);
        }
        return customerIssueRepository.save(issue);
    }
}
