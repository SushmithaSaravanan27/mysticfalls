package com.amazecare.controller;

import com.amazecare.entity.Doctor;
import com.amazecare.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.amazecare.dto.DoctorDTO;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor

public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping("/appointments/{doctorId}")
    public ResponseEntity<?> getAppointments(@PathVariable Long doctorId) {
        return doctorService.getAppointments(doctorId);
    }

    @PutMapping("/cancel/{appointmentId}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long appointmentId) {
        return doctorService.cancelAppointment(appointmentId);
    }

    @PostMapping("/prescribe")
    public ResponseEntity<?> addPrescription(@RequestBody com.amazecare.dto.PrescriptionDTO dto) {
        return doctorService.addPrescription(dto);
    }

    @GetMapping("/completed-consultations/{doctorId}")
    public ResponseEntity<?> getCompletedConsultations(@PathVariable Long doctorId) {
        return doctorService.getCompletedConsultations(doctorId);
    }

    // ✅ New: Get all doctors
    @GetMapping("/all")
    public List<Doctor> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    // ✅ New: Get all unique specialties
    @GetMapping("/specialties")
    public List<String> getAllSpecialties() {
        return doctorService.getAllSpecialties();
    }
 // ✅ New endpoint: Get doctor ID + name only
    @GetMapping("/id-name")
    public ResponseEntity<List<DoctorDTO>> getDoctorIdAndNames() {
        return ResponseEntity.ok(doctorService.getDoctorIdAndNames());
    }

}
