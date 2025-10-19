package com.amazecare.service.impl;
import java.util.List;

import com.amazecare.dto.DoctorDTO;
import com.amazecare.dto.PatientDTO;
import com.amazecare.entity.Doctor;
import com.amazecare.entity.Patient;
import com.amazecare.entity.User;
import com.amazecare.repository.DoctorRepository;
import com.amazecare.repository.PatientRepository;
import com.amazecare.repository.UserRepository;
import com.amazecare.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<Map<String, String>> addDoctor(DoctorDTO doctorDTO) {
        // Step 1: Check if username exists
        if (userRepository.findByUsername(doctorDTO.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Username already exists."));
        }

        // Step 2: Create user for the doctor
        User user = new User();
        user.setUsername(doctorDTO.getUsername());
        user.setPassword(passwordEncoder.encode(doctorDTO.getPassword()));
        user.setRole("DOCTOR");
        userRepository.save(user);

        // Step 3: Create and link doctor
        Doctor doctor = new Doctor();
        doctor.setName(doctorDTO.getName());
        doctor.setSpecialty(doctorDTO.getSpecialty());
        doctor.setExperience(doctorDTO.getExperience());
        doctor.setQualification(doctorDTO.getQualification());
        doctor.setDesignation(doctorDTO.getDesignation());
        doctor.setPhotoUrl(doctorDTO.getPhotoUrl()); // ✅ NEW: Set photo URL
        doctor.setUser(user); // ✅ Link user

        doctorRepository.save(doctor); // ✅ Save doctor

        return ResponseEntity.ok(Collections.singletonMap("message", "Doctor is added successfully"));
    }

    @Override
    public ResponseEntity<Map<String, String>> addPatient(PatientDTO patientDTO) {
        // Step 1: Check if username exists
        if (userRepository.findByUsername(patientDTO.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Username already exists."));
        }

        // Step 2: Create user for the patient
        User user = new User();
        user.setUsername(patientDTO.getUsername());
        user.setPassword(passwordEncoder.encode(patientDTO.getPassword()));
        user.setRole("PATIENT");
        userRepository.save(user);

        // Step 3: Create and link patient
        Patient patient = new Patient();
        patient.setFullName(patientDTO.getFullName());
        patient.setGender(patientDTO.getGender());
        patient.setDob(patientDTO.getDob());
        patient.setContactNumber(patientDTO.getContactNumber());
        patient.setHealthIssue(patientDTO.getHealthIssue());
        patient.setUser(user); // ✅ Link user

        patientRepository.save(patient);
        return ResponseEntity.ok(Collections.singletonMap("message", "Patient is added successfully."));
    }

    @Override
    public ResponseEntity<Map<String, String>> deleteDoctor(Long id) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(id);
        if (doctorOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Collections.singletonMap("message", "Doctor not found with ID: " + id));
        }
        doctorRepository.deleteById(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "Doctor is deleted successfully."));
    }

    @Override
    public ResponseEntity<Map<String, String>> deletePatient(Long id) {
        Optional<Patient> patientOpt = patientRepository.findById(id);
        if (patientOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Collections.singletonMap("message", "Patient not found with ID: " + id));
        }
        patientRepository.deleteById(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "Patient is deleted successfully."));
    }
    @Override
    public List<PatientDTO> getAllPatientIdsAndNames() {
        List<Patient> patients = patientRepository.findAll();
        return patients.stream().map(p -> {
            PatientDTO dto = new PatientDTO();
            dto.setId(p.getId());
            dto.setName(p.getFullName()); // or p.getName()
            return dto;
        }).toList();
    }

}
