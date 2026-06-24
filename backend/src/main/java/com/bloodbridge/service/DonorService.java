package com.bloodbridge.service;

import com.bloodbridge.exception.BadRequestException;
import com.bloodbridge.exception.ResourceNotFoundException;
import com.bloodbridge.model.*;
import com.bloodbridge.payload.request.AppointmentRequest;
import com.bloodbridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DonorService {

    private final DonorRepository donorRepository;
    private final AppointmentRepository appointmentRepository;
    private final DonationRepository donationRepository;
    private final BloodBankRepository bloodBankRepository;
    private final NotificationRepository notificationRepository;

    public Donor getDonorByUserId(Long userId) {
        return donorRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found for user: " + userId));
    }

    @Transactional
    public Donor updateProfile(Long userId, Donor updatedData) {
        Donor donor = getDonorByUserId(userId);
        if (updatedData.getPhone() != null) donor.setPhone(updatedData.getPhone());
        if (updatedData.getAddress() != null) donor.setAddress(updatedData.getAddress());
        if (updatedData.getCity() != null) donor.setCity(updatedData.getCity());
        if (updatedData.getLatitude() != null) donor.setLatitude(updatedData.getLatitude());
        if (updatedData.getLongitude() != null) donor.setLongitude(updatedData.getLongitude());
        if (updatedData.getEmergencyContact() != null) donor.setEmergencyContact(updatedData.getEmergencyContact());
        return donorRepository.save(donor);
    }

    @Transactional
    public Appointment bookAppointment(Long userId, AppointmentRequest request) {
        Donor donor = getDonorByUserId(userId);

        if (!donor.getEligibilityStatus()) {
            throw new BadRequestException("You are not eligible to donate. Next eligible date: " + donor.getNextEligibleDate());
        }

        BloodBank bloodBank = bloodBankRepository.findById(request.getBloodBankId())
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found"));

        if (bloodBank.getApprovalStatus() != BloodBank.ApprovalStatus.APPROVED) {
            throw new BadRequestException("Blood bank is not approved.");
        }

        // Check duplicate booking
        boolean alreadyBooked = appointmentRepository.existsByDonorAndAppointmentDateAndStatusNot(
                donor, request.getAppointmentDate(), Appointment.AppointmentStatus.CANCELLED);
        if (alreadyBooked) {
            throw new BadRequestException("You already have an appointment on this date.");
        }

        Appointment appointment = Appointment.builder()
                .donor(donor)
                .bloodBank(bloodBank)
                .appointmentDate(request.getAppointmentDate())
                .timeSlot(request.getTimeSlot())
                .status(Appointment.AppointmentStatus.PENDING)
                .notes(request.getNotes())
                .build();

        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getMyAppointments(Long userId) {
        Donor donor = getDonorByUserId(userId);
        return appointmentRepository.findByDonorOrderByCreatedAtDesc(donor);
    }

    public List<Donation> getDonationHistory(Long userId) {
        Donor donor = getDonorByUserId(userId);
        return donationRepository.findByDonorOrderByDonationDateDesc(donor);
    }

    public List<Notification> getMyNotifications(Long userId) {
        Donor donor = getDonorByUserId(userId);
        return notificationRepository.findByUserOrderByCreatedAtDesc(donor.getUser());
    }

    @Transactional
    public Notification respondToEmergency(Long notificationId, Notification.DonorResponse response) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setDonorResponse(response);
        notification.setStatus(Notification.NotificationStatus.READ);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void recalculateEligibility() {
        List<Donor> donors = donorRepository.findAll();
        LocalDate today = LocalDate.now();
        for (Donor donor : donors) {
            if (donor.getNextEligibleDate() != null && !today.isBefore(donor.getNextEligibleDate())) {
                donor.setEligibilityStatus(true);
                donorRepository.save(donor);
            }
        }
    }
}
