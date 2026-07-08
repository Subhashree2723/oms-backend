package com.oms.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

public class AuthDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AuthResponse {
        private String token;
        private String username;
        private String role;
        private Long customerId; // null for admin
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank
        private String username;
        @NotBlank
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
        @Email @NotBlank
        private String email;
        @NotBlank
        private String fullName;
        @NotBlank
        private String phone;
        private String address;
        private String city;
        private String state;
        private String pincode;
    }
}
