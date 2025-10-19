package com.amazecare.service.impl;

import com.amazecare.dto.DoctorDTO;
import com.amazecare.dto.AppointmentDTO;
import com.amazecare.dto.PrescriptionDTO;
import com.amazecare.entity.Appointment;
import com.amazecare.entity.MedicalRecord;
import com.amazecare.entity.Prescription;
import com.amazecare.entity.Doctor;
import com.amazecare.entity.Patient;
import com.amazecare.repository.AppointmentRepository;
import com.amazecare.repository.MedicalRecordRepository;
import com.amazecare.repository.PrescriptionRepository;
import com.amazecare.repository.DoctorRepository;
import com.amazecare.repository.PatientRepository;
import com.amazecare.service.DoctorService;
import com.amazecare.service.EmailService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final AppointmentRepository appointmentRepo;
    private final PrescriptionRepository prescriptionRepo;
    private final MedicalRecordRepository medicalRecordRepo;
    private final DoctorRepository doctorRepo;
    private final PatientRepository patientRepository; // ✅ Added
    private final EmailService emailService;           // ✅ Added

    @Override
    public ResponseEntity<?> getAppointments(Long doctorId) {
        List<Appointment> appointments = appointmentRepo.findByDoctorId(doctorId);

        List<AppointmentDTO> dtos = appointments.stream().map(appointment -> {
            AppointmentDTO dto = new AppointmentDTO();
            dto.setAppointmentId(appointment.getId());
            dto.setDoctorId(appointment.getDoctor().getId());
            dto.setPatientId(appointment.getPatient().getId());
            dto.setPreferredDateTime(appointment.getAppointmentDateTime());
            dto.setSymptoms(appointment.getSymptoms());
            dto.setStatus(appointment.getStatus());

            dto.setPatientName(appointment.getPatient().getFullName());
            dto.setPatientContact(appointment.getPatient().getContactNumber());

            return dto;
        }).toList();

        return ResponseEntity.ok(dtos);
    }

    @Override
    public ResponseEntity<?> getCompletedConsultations(Long doctorId) {
        List<Appointment> appointments = appointmentRepo.findByDoctorIdAndStatus(doctorId, "COMPLETED");

        List<Map<String, Object>> results = appointments.stream().map(appointment -> {
            Map<String, Object> map = new HashMap<>();

            map.put("appointmentId", appointment.getId());
            map.put("appointmentDateTime", appointment.getAppointmentDateTime());

            Map<String, Object> patientMap = new HashMap<>();
            patientMap.put("fullName", appointment.getPatient().getFullName());
            patientMap.put("contactNumber", appointment.getPatient().getContactNumber());
            map.put("patient", patientMap);

            MedicalRecord record = appointment.getMedicalRecord();
            if (record != null) {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("diagnosis", record.getDiagnosis());
                recordMap.put("treatmentPlan", record.getTreatmentPlan());
                recordMap.put("recommendedTests", record.getRecommendedTests());
                map.put("medicalRecord", recordMap);
            } else {
                map.put("medicalRecord", null);
            }

            Prescription prescription = appointment.getPrescription();
            if (prescription != null) {
                Map<String, Object> presMap = new HashMap<>();
                presMap.put("medicineDetails", prescription.getMedicineDetails());
                map.put("prescription", presMap);
            } else {
                map.put("prescription", null);
            }

            return map;
        }).toList();

        return ResponseEntity.ok(results);
    }

    @Override
    public ResponseEntity<?> addPrescription(PrescriptionDTO dto) {
        Appointment appointment = appointmentRepo.findById(dto.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        MedicalRecord record = new MedicalRecord();
        record.setAppointment(appointment);
        record.setDiagnosis(dto.getDiagnosis());
        record.setTreatmentPlan(dto.getTreatmentPlan());
        record.setRecommendedTests(dto.getRecommendedTests());
        medicalRecordRepo.save(record);

        Prescription prescription = new Prescription();
        prescription.setAppointment(appointment);
        prescription.setSymptoms(dto.getSymptoms());
        prescription.setDiagnosis(dto.getDiagnosis());
        prescription.setTreatmentPlan(dto.getTreatmentPlan());
        prescription.setRecommendedTests(dto.getRecommendedTests());
        prescription.setMedicineDetails(dto.getPrescribedMedicine());
        prescriptionRepo.save(prescription);

        appointment.setStatus("COMPLETED");
        appointmentRepo.save(appointment);

        // ✅ Email sending logic
        Patient patient = appointment.getPatient();
        if (patient != null && patient.getEmail() != null) {
            String subject = "Your Prescription from AmazeCare";
            String body = "Dear " + patient.getFullName() + ",\n\n"
                    + "Here is your prescription summary:\n\n"
                    + "Symptoms: " + dto.getSymptoms() + "\n"
                    + "Diagnosis: " + dto.getDiagnosis() + "\n"
                    + "Treatment Plan: " + dto.getTreatmentPlan() + "\n"
                    + "Recommended Tests: " + dto.getRecommendedTests() + "\n"
                    + "Prescribed Medicines: " + dto.getPrescribedMedicine() + "\n\n"
                    + "Stay healthy,\nAmazeCare Team";

            emailService.sendPrescriptionEmail(patient.getEmail(), subject, body);
        }

        return ResponseEntity.ok("Consultation completed, prescription saved, and email sent.");
    }

    @Override
    public ResponseEntity<?> cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setStatus("CANCELLED");
        appointmentRepo.save(appointment);
        return ResponseEntity.ok("Appointment cancelled.");
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return doctorRepo.findAll();
    }

    @Override
    public List<String> getAllSpecialties() {
        return doctorRepo.findDistinctSpecialties();
    }

    @Override
    public List<DoctorDTO> getDoctorIdAndNames() {
        List<Doctor> doctors = doctorRepo.findAll();
        return doctors.stream().map(d -> {
            DoctorDTO dto = new DoctorDTO();
            dto.setId(d.getId());
            dto.setName(d.getName());
            return dto;
        }).toList();
    }
}  