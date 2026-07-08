package com.oms.backend.serviceimpl;

import com.oms.backend.dto.AuthDtos.*;
import com.oms.backend.entity.Customer;
import com.oms.backend.entity.Role;
import com.oms.backend.entity.User;
import com.oms.backend.exception.BadRequestException;
import com.oms.backend.repository.CustomerRepository;
import com.oms.backend.repository.RoleRepository;
import com.oms.backend.repository.UserRepository;
import com.oms.backend.security.JwtUtil;
import com.oms.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        if (!user.getEnabled()) {
            throw new BadRequestException("Account is disabled");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().getName());
        Long customerId = customerRepository.findByUserId(user.getId()).map(Customer::getId).orElse(null);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().getName())
                .customerId(customerId)
                .build();
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new BadRequestException("ROLE_CUSTOMER not configured"));

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(customerRole)
                .enabled(true)
                .build();
        user = userRepository.save(user);

        Customer customer = Customer.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .build();
        customer = customerRepository.save(customer);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().getName());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().getName())
                .customerId(customer.getId())
                .build();
    }
}
