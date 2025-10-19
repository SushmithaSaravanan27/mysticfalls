package com.amazecare.repository;

import com.amazecare.entity.Prescription;
import com.amazecare.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
	Optional<Prescription> findByAppointment(Appointment appointment);

}
