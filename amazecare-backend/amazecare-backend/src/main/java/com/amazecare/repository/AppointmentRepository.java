package com.amazecare.repository;

import com.amazecare.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorIdAndStatus(Long doctorId, String status);
 
    List<Appointment> findByPatientIdAndStatus(Long patientId, String status);

}