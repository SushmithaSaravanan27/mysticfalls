package com.amazecare.controller;

import com.amazecare.dto.AppointmentDTO;
import com.amazecare.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    // ✅ FIXED: Book appointment with JSON response
    @PostMapping("/book")
    public ResponseEntity<Map<String, Object>> bookAppointment(@RequestBody AppointmentDTO appointmentDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            patientService.bookAppointment(appointmentDTO);
            response.put("message", "Appointment booked");
            response.put("status", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Failed to book appointment");
            response.put("status", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/appointments/{patientId}")
    public ResponseEntity<?> viewAppointments(@PathVariable Long patientId) {
        return patientService.viewAppointments(patientId);
    }
    @GetMapping("/medical-history/{patientId}")
    public ResponseEntity<?> getMedicalHistory(@PathVariable Long patientId) {
        return patientService.getMedicalHistory(patientId);
    }


    @GetMapping("/doctors")
    public ResponseEntity<?> getAllDoctors() {
        return patientService.getAllDoctors();
    }

    @DeleteMapping("/cancel/{appointmentId}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long appointmentId) {
        return patientService.cancelAppointment(appointmentId);
    }
}
