package com.amazecare.repository;

import com.amazecare.entity.MedicalRecord;
import com.amazecare.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
	Optional<MedicalRecord> findByAppointment(Appointment appointment);
	boolean existsByAppointmentId(Long appointmentId);
}
