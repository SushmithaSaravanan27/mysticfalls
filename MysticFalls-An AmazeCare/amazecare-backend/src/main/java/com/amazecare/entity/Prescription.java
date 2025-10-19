package com.amazecare.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Appointment appointment;

    private String symptoms;
    private String diagnosis;
    private String treatmentPlan;
    private String recommendedTests;
    private String medicineDetails;
}
