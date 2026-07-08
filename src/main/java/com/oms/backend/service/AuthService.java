package com.oms.backend.service;

import com.oms.backend.dto.AuthDtos.*;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}
