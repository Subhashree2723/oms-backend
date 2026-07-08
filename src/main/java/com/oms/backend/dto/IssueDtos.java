package com.oms.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class IssueDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateIssueRequest {
        @NotBlank
        private String subject;
        @NotBlank
        private String description;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ReplyRequest {
        @NotBlank
        private String message;
    }
}
