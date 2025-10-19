package com.amazecare.service;
import com.amazecare.dto.DoctorDTO;

import com.amazecare.dto.PrescriptionDTO;
import com.amazecare.entity.Doctor;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DoctorService {
    ResponseEntity<?> getAppointments(Long doctorId);
    ResponseEntity<?> addPrescription(PrescriptionDTO dto);
    ResponseEntity<?> cancelAppointment(Long appointmentId);
    ResponseEntity<?> getCompletedConsultations(Long doctorId);
    List<Doctor> getAllDoctors();
    List<String> getAllSpecialties();
    List<DoctorDTO> getDoctorIdAndNames();  

}
