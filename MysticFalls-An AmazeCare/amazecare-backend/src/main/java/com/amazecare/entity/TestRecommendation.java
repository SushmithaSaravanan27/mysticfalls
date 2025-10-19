package com.amazecare.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class TestRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String testName;
    private String remarks;

    @OneToOne
    private Appointment appointment;
}
