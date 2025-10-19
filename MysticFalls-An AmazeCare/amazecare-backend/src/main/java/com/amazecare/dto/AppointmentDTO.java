package com.amazecare.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentDTO {
    private Long doctorId;
    private Long patientId;
    private String symptoms;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime preferredDateTime;

    private String doctorName;
    private String specialty;

    private Long appointmentId;
    private String status;

    private String patientName;
    private String patientContact;
}
