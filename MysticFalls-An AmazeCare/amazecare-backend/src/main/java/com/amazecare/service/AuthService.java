package com.amazecare.service;

import com.amazecare.dto.LoginRequest;
import com.amazecare.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<?> register(RegisterRequest request);
    ResponseEntity<?> login(LoginRequest request);
    ResponseEntity<?> forgetPassword(String username, String newPassword);
}
