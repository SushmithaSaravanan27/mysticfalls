package com.amazecare.repository;
import com.amazecare.entity.User;

import com.amazecare.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
	  Patient findByUser(User user); 
}
