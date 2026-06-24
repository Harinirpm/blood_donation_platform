package com.bloodbridge.service;

import com.bloodbridge.exception.BadRequestException;
import com.bloodbridge.exception.ResourceNotFoundException;
import com.bloodbridge.model.*;
import com.bloodbridge.payload.request.DonationRequest;
import com.bloodbridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BloodBankService {

    private final BloodBankRepository bloodBankRepository;
    private final BloodInventoryRepository inventoryRepository;
    private final DonorRepository donorRepository;
    private final DonationRepository donationRepository;
    private final HospitalRequestRepository requestRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public BloodBank getBloodBankByUserId(Long userId) {
        return bloodBankRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found for user: " + userId));
    }

    public List<BloodInventory> getInventory(Long bloodBankId) {
        BloodBank bank = bloodBankRepository.findById(bloodBankId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found: " + bloodBankId));
        return inventoryRepository.findByBloodBank(bank);
    }

    @Transactional
    public BloodInventory updateInventory(Long bloodBankId, BloodGroup bloodGroup, int delta) {
        BloodBank bank = bloodBankRepository.findById(bloodBankId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found: " + bloodBankId));

        BloodInventory inventory = inventoryRepository
                .findByBloodBankAndBloodGroup(bank, bloodGroup)
                .orElseGet(() -> BloodInventory.builder()
                        .bloodBank(bank)
                        .bloodGroup(bloodGroup)
                        .unitsAvailable(0)
                        .build());

        int newUnits = inventory.getUnitsAvailable() + delta;
        if (newUnits < 0) throw new BadRequestException("Insufficient blood units in inventory");
        inventory.setUnitsAvailable(newUnits);
        inventoryRepository.save(inventory);

        // Broadcast real-time inventory update
        messagingTemplate.convertAndSend(
                "/topic/inventory/" + bloodBankId,
                inventory
        );
        return inventory;
    }

    @Transactional
    public Donation recordDonation(Long bloodBankId, DonationRequest req) {
        BloodBank bank = bloodBankRepository.findById(bloodBankId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found"));
        Donor donor = donorRepository.findById(req.getDonorId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found"));

        if (!donor.getEligibilityStatus()) {
            throw new BadRequestException("Donor is not eligible to donate at this time.");
        }

        LocalDate donationDate = req.getDonationDate();
        LocalDate nextEligible = donationDate.plusDays(180);

        Donation donation = Donation.builder()
                .donor(donor)
                .bloodBank(bank)
                .bloodGroup(req.getBloodGroup())
                .unitsDonated(req.getUnitsDonated())
                .donationDate(donationDate)
                .nextEligibleDate(nextEligible)
                .donationType(req.getDonationType())
                .certificateId("CERT-" + System.currentTimeMillis())
                .build();

        if (req.getHospitalRequestId() != null) {
            HospitalRequest hr = requestRepository.findById(req.getHospitalRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
            donation.setHospitalRequest(hr);
        }

        donationRepository.save(donation);

        // Update donor eligibility
        donor.setLastDonationDate(donationDate);
        donor.setNextEligibleDate(nextEligible);
        donor.setEligibilityStatus(false);
        donor.setTotalDonations(donor.getTotalDonations() + 1);
        donorRepository.save(donor);

        // Update inventory
        updateInventory(bloodBankId, req.getBloodGroup(), req.getUnitsDonated());

        return donation;
    }

    public List<Appointment> getPendingAppointments(Long bloodBankId) {
        BloodBank bank = bloodBankRepository.findById(bloodBankId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found"));
        return appointmentRepository.findByBloodBankAndStatus(bank, Appointment.AppointmentStatus.PENDING);
    }

    @Transactional
    public Appointment updateAppointmentStatus(Long appointmentId, Appointment.AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }

    public List<BloodBank> getAllApprovedBloodBanks() {
        return bloodBankRepository.findByApprovalStatus(BloodBank.ApprovalStatus.APPROVED);
    }

    public List<BloodBank> getNearbyBloodBanks(double lat, double lng, double radiusKm) {
        return bloodBankRepository.findNearbyBloodBanks(lat, lng, radiusKm);
    }

    public List<Donor> getNearbyDonors(double lat, double lng, double radiusKm) {
        return donorRepository.findNearbyDonors(lat, lng, radiusKm);
    }

    @Transactional
    public void sendEmergencyNotifications(Long requestId) {
        HospitalRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        BloodBank bank = request.getBloodBank();
        double lat = bank.getLatitude() != null ? bank.getLatitude() : 0;
        double lng = bank.getLongitude() != null ? bank.getLongitude() : 0;

        List<Donor> eligibleDonors = donorRepository.findEligibleDonorsByBloodGroupAndLocation(
                request.getBloodGroup(), lat, lng, 50.0);

        for (Donor donor : eligibleDonors) {
            String message = String.format(
                "🚨 Emergency Blood Requirement!\nBlood Group: %s\nRequired Units: %d\nBlood Bank: %s\nUrgency: %s",
                request.getBloodGroup().getDisplay(),
                request.getRequiredUnits(),
                bank.getName(),
                request.getUrgencyLevel()
            );

            Notification notification = Notification.builder()
                    .user(donor.getUser())
                    .hospitalRequest(request)
                    .title("Emergency Blood Request")
                    .message(message)
                    .type(Notification.NotificationType.EMERGENCY_REQUEST)
                    .status(Notification.NotificationStatus.UNREAD)
                    .donorResponse(Notification.DonorResponse.PENDING)
                    .build();
            notificationRepository.save(notification);

            // Push via WebSocket
            messagingTemplate.convertAndSendToUser(
                    donor.getUser().getEmail(),
                    "/queue/notifications",
                    notification
            );
        }

        request.setStatus(HospitalRequest.RequestStatus.SEARCHING_DONORS);
        requestRepository.save(request);
    }
}
