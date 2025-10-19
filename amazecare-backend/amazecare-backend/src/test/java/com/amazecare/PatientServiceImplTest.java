package com.amazecare;

import com.amazecare.dto.AppointmentDTO;
import com.amazecare.entity.Appointment;
import com.amazecare.entity.Doctor;
import com.amazecare.entity.Patient;
import com.amazecare.repository.*;
import com.amazecare.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @InjectMocks
    private PatientServiceImpl patientService;

    @Mock
    private AppointmentRepository appointmentRepo;

    @Mock
    private DoctorRepository doctorRepo;

    @Mock
    private PatientRepository patientRepo;

    @Mock
    private MedicalRecordRepository medicalRecordRepo;

    @Mock
    private PrescriptionRepository prescriptionRepo;

    @Test
    void testBookAppointment_Success() {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setDoctorId(1L);
        dto.setPatientId(2L);
        dto.setSymptoms("Fever");
        dto.setPreferredDateTime(LocalDateTime.now());

        Doctor doctor = new Doctor();
        doctor.setId(1L);
        Patient patient = new Patient();
        patient.setId(2L);

        when(doctorRepo.findById(1L)).thenReturn(Optional.of(doctor));
        when(patientRepo.findById(2L)).thenReturn(Optional.of(patient));
        when(appointmentRepo.save(any(Appointment.class))).thenReturn(new Appointment());

        ResponseEntity<?> response = patientService.bookAppointment(dto);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo("Appointment booked successfully.");
    }

    @Test
    void testBookAppointment_DoctorNotFound() {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setDoctorId(1L);
        dto.setPatientId(2L);

        when(doctorRepo.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = patientService.bookAppointment(dto);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).isEqualTo("Doctor not found with ID: 1");
    }

    @Test
    void testViewAppointments() {
        Long patientId = 2L;
        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("Dr. Smith");
        doctor.setSpecialty("Cardiology");

        Patient patient = new Patient();
        patient.setId(2L);

        Appointment appt = new Appointment();
        appt.setId(10L);
        appt.setDoctor(doctor);
        appt.setPatient(patient);
        appt.setAppointmentDateTime(LocalDateTime.now());
        appt.setSymptoms("Fever");
        appt.setStatus("PENDING");

        when(appointmentRepo.findByPatientId(patientId)).thenReturn(List.of(appt));

        ResponseEntity<?> response = patientService.viewAppointments(patientId);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).asList().hasSize(1);
    }

    @Test
    void testCancelAppointment_NotFound() {
        Long appointmentId = 5L;

        when(appointmentRepo.existsById(appointmentId)).thenReturn(false);

        ResponseEntity<?> response = patientService.cancelAppointment(appointmentId);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(((Map<?, ?>) response.getBody()).get("message")).isEqualTo("Appointment not found.");
        assertThat(((Map<?, ?>) response.getBody()).get("status")).isEqualTo(false);
    }

    @Test
    void testGetAllDoctors() {
        Doctor doctor1 = new Doctor();
        doctor1.setId(1L);
        doctor1.setName("Dr. A");

        Doctor doctor2 = new Doctor();
        doctor2.setId(2L);
        doctor2.setName("Dr. B");

        when(doctorRepo.findAll()).thenReturn(List.of(doctor1, doctor2));

        ResponseEntity<?> response = patientService.getAllDoctors();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(((List<?>) response.getBody())).hasSize(2);
    }
}
