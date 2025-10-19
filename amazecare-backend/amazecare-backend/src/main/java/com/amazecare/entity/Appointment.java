package com.amazecare.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Patient patient;

    @ManyToOne
    private Doctor doctor;


    private String symptoms;

    private LocalDateTime appointmentDateTime;

    private String status; // PENDING, CONFIRMED, COMPLETED, CANCELLED
    @OneToOne(mappedBy = "appointment")
    private MedicalRecord medicalRecord;

    @OneToOne(mappedBy = "appointment")
    private Prescription prescription;

}
