package com.amazecare.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String specialty;
    private int experience;
    private String qualification;
    private String designation;

    private String photoUrl; // ✅ Add this field

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
