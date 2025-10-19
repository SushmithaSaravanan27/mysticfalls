package com.amazecare.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String role; // ADMIN, PATIENT, DOCTOR
    private String email;   
}
