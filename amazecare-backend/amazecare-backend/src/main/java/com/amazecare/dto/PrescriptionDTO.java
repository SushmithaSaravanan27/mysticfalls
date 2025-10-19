package com.amazecare.dto;

import lombok.Data;

@Data
public class PrescriptionDTO {
    private Long appointmentId;
    private String symptoms;
    private String diagnosis;
    private String treatmentPlan;
    private String recommendedTests;
    private String prescribedMedicine;
}
