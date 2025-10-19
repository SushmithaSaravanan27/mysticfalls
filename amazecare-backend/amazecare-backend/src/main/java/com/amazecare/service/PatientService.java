package com.amazecare.service;

import com.amazecare.dto.AppointmentDTO;
import org.springframework.http.ResponseEntity;

public interface PatientService {
    ResponseEntity<?> bookAppointment(AppointmentDTO dto);
    ResponseEntity<?> viewAppointments(Long patientId);
    ResponseEntity<?> getMedicalHistory(Long patientId);
    ResponseEntity<?> cancelAppointment(Long appointmentId);
    ResponseEntity<?> getAllDoctors();

}
