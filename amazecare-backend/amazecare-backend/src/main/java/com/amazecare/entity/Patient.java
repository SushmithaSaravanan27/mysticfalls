package com.amazecare.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String gender;
    private String dob;
    private String contactNumber;
    private String healthIssue;
    private String email;

@OneToOne
@JoinColumn(name = "user_id")
private User user;
}