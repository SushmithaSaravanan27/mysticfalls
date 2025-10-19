package com.amazecare.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Appointment appointment;

    private String diagnosis;
    private String treatmentPlan;
    private String recommendedTests;
}
