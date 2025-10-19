package com.amazecare.dto;

import lombok.Data;

@Data
public class DoctorDTO {
    private String name;
    private String specialty;
    private int experience;
    private String qualification;
    private String designation;
    private String username;
    private String password;
    private Long id;
    private String photoUrl; 
    private String email;

}
