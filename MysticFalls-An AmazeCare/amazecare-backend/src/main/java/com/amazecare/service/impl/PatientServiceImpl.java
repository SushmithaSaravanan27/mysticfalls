package com.amazecare.service.impl;

import com.amazecare.dto.AppointmentDTO;
import com.amazecare.entity.Appointment;
import com.amazecare.entity.Doctor;
import com.amazecare.entity.MedicalRecord;
import com.amazecare.entity.Patient;
import com.amazecare.entity.Prescription;
import com.amazecare.repository.AppointmentRepository;
import com.amazecare.repository.DoctorRepository;
import com.amazecare.repository.MedicalRecordRepository;
import com.amazecare.repository.PatientRepository;
import com.amazecare.repository.PrescriptionRepository;
import com.amazecare.service.PatientService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final AppointmentRepository appointmentRepo;
    private final DoctorRepository doctorRepo;
    private final PatientRepository patientRepo;
    private final MedicalRecordRepository medicalRecordRepo;
    private final PrescriptionRepository prescriptionRepo;

    @Override
    public ResponseEntity<?> bookAppointment(AppointmentDTO dto) {
        Optional<Doctor> doctorOpt = doctorRepo.findById(dto.getDoctorId());
        Optional<Patient> patientOpt = patientRepo.findById(dto.getPatientId());

        if (doctorOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Doctor not found with ID: " + dto.getDoctorId());
        }

        if (patientOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Patient not found with ID: " + dto.getPatientId());
        }

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctorOpt.get());
        appointment.setPatient(patientOpt.get());
        appointment.setSymptoms(dto.getSymptoms());
        appointment.setAppointmentDateTime(dto.getPreferredDateTime());
        appointment.setStatus("PENDING");

        appointmentRepo.save(appointment);
        return ResponseEntity.ok("Appointment booked successfully.");
    }

    @Override
    public ResponseEntity<?> viewAppointments(Long patientId) {
        List<Appointment> appointments = appointmentRepo.findByPatientId(patientId);

        List<AppointmentDTO> dtoList = appointments.stream().map(appointment -> {
            AppointmentDTO dto = new AppointmentDTO();
            dto.setAppointmentId(appointment.getId());
            dto.setDoctorId(appointment.getDoctor().getId());
            dto.setPatientId(appointment.getPatient().getId());
            dto.setPreferredDateTime(appointment.getAppointmentDateTime());
            dto.setSymptoms(appointment.getSymptoms());
            dto.setDoctorName(appointment.getDoctor().getName());
            dto.setSpecialty(appointment.getDoctor().getSpecialty());
            dto.setStatus(appointment.getStatus());
            return dto;
        }).toList();

        return ResponseEntity.ok(dtoList);
    }

    @Override
    public ResponseEntity<?> getMedicalHistory(Long patientId) {
        List<Appointment> appointments = appointmentRepo.findByPatientIdAndStatus(patientId, "COMPLETED");

        List<Map<String, Object>> history = appointments.stream().map(appt -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", appt.getAppointmentDateTime());

            Doctor doctor = appt.getDoctor();
            map.put("doctor", doctor.getName()); // or doctor.getFullName() if exists

            MedicalRecord med = medicalRecordRepo.findByAppointment(appt).orElse(null);
            Prescription pres = prescriptionRepo.findByAppointment(appt).orElse(null);

            map.put("diagnosis", med != null ? med.getDiagnosis() : "-");
            map.put("treatmentPlan", med != null ? med.getTreatmentPlan() : "-");
            map.put("recommendedTests", med != null ? med.getRecommendedTests() : "-");
            map.put("prescription", pres != null ? pres.getMedicineDetails() : "-");

            return map;
        }).toList();

        return ResponseEntity.ok(history);
    }

    @Override
    public ResponseEntity<?> cancelAppointment(Long appointmentId) {
        if (!appointmentRepo.existsById(appointmentId)) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Appointment not found.");
            error.put("status", false);
            return ResponseEntity.badRequest().body(error);
        }

        appointmentRepo.deleteById(appointmentId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Appointment cancelled successfully.");
        response.put("status", true);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> getAllDoctors() {
        List<Doctor> doctors = doctorRepo.findAll();
        return ResponseEntity.ok(doctors);
    }
}
