package com.amazecare.service;
import java.util.List;

import com.amazecare.dto.DoctorDTO;
import com.amazecare.dto.PatientDTO;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface AdminService {
    ResponseEntity<Map<String, String>> addDoctor(DoctorDTO doctorDTO);
    ResponseEntity<Map<String, String>> addPatient(PatientDTO patientDTO);
    ResponseEntity<Map<String, String>> deleteDoctor(Long id);
    ResponseEntity<Map<String, String>> deletePatient(Long id);
    List<PatientDTO> getAllPatientIdsAndNames();

}