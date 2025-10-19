package com.amazecare.repository;

import com.amazecare.entity.User;
import com.amazecare.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Doctor findByUser(User user);

    // ✅ New: Find unique specialties
    @Query("SELECT DISTINCT d.specialty FROM Doctor d")
    List<String> findDistinctSpecialties();
}
