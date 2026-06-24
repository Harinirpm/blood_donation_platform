package com.bloodbridge.repository;

import com.bloodbridge.model.Appointment;
import com.bloodbridge.model.BloodBank;
import com.bloodbridge.model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDonorOrderByCreatedAtDesc(Donor donor);
    List<Appointment> findByBloodBankOrderByAppointmentDateAsc(BloodBank bloodBank);
    List<Appointment> findByBloodBankAndAppointmentDate(BloodBank bloodBank, LocalDate date);
    List<Appointment> findByBloodBankAndStatus(BloodBank bloodBank, Appointment.AppointmentStatus status);

    boolean existsByDonorAndAppointmentDateAndStatusNot(
            Donor donor, LocalDate date, Appointment.AppointmentStatus status);
}
