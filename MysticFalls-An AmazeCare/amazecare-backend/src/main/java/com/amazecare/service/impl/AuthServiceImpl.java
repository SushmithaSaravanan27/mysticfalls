package com.amazecare.service.impl;

import com.amazecare.dto.LoginRequest;
import com.amazecare.dto.RegisterRequest;
import com.amazecare.entity.Doctor;
import com.amazecare.entity.Patient;
import com.amazecare.entity.User;
import com.amazecare.repository.DoctorRepository;
import com.amazecare.repository.PatientRepository;
import com.amazecare.repository.UserRepository;
import com.amazecare.security.JwtUtil;
import com.amazecare.service.AuthService;
import com.amazecare.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private EmailService emailService;

    @Override
    public ResponseEntity<?> register(RegisterRequest request) {
        Map<String, String> response = new HashMap<>();

        if (userRepo.findByUsername(request.getUsername()).isPresent()) {
            response.put("message", "Username already exists.");
            return ResponseEntity.status(409).body(response);
        }

        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            response.put("message", "Email already exists.");
            return ResponseEntity.status(409).body(response);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole().toUpperCase());
        user.setEmail(request.getEmail());

        userRepo.save(user);

        response.put("message", "User registered successfully.");
        return ResponseEntity.status(201).body(response);
    }

    @Override
    public ResponseEntity<?> login(LoginRequest request) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        User user = userRepo.findByUsername(request.getUsername()).orElse(null);
        if (user == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "User not found.");
            return ResponseEntity.status(401).body(errorResponse);
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User logged in successfully.");
        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("id", user.getId());
        response.put("email", user.getEmail());

        if ("PATIENT".equalsIgnoreCase(user.getRole())) {
            Patient patient = patientRepo.findByUser(user);
            if (patient != null) {
                response.put("patientId", patient.getId());
            }
        } else if ("DOCTOR".equalsIgnoreCase(user.getRole())) {
            Doctor doctor = doctorRepo.findByUser(user);
            if (doctor != null) {
                response.put("doctorId", doctor.getId());
            }
        }

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> forgetPassword(String username, String newPassword) {
        Map<String, String> response = new HashMap<>();
        User user = userRepo.findByUsername(username).orElse(null);

        if (user == null) {
            response.put("message", "User not found.");
            return ResponseEntity.status(404).body(response);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        // ✅ Send email confirmation to the user's registered email
        emailService.sendPrescriptionEmail(
                user.getEmail(),
                "Password Reset Confirmation",
                "Hello " + user.getUsername() + ", your password has been successfully reset.");

        response.put("message", "Password reset successfully.");
        return ResponseEntity.ok(response);
    }
}
