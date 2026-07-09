package com.oms.backend.controller;

import com.oms.backend.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping({"/", "/health"})
    public ApiResponse health() {
        return ApiResponse.builder()
                .success(true)
                .message("OMS backend is up and running")
                .data(null)
                .build();
    }
}
