package com.bloodbridge.controller;

import com.bloodbridge.model.*;
import com.bloodbridge.payload.request.AppointmentRequest;
import com.bloodbridge.payload.response.ApiResponse;
import com.bloodbridge.security.UserPrincipal;
import com.bloodbridge.service.BloodBankService;
import com.bloodbridge.service.CertificateService;
import com.bloodbridge.service.DonorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donor")
@RequiredArgsConstructor
public class DonorController {

    private final DonorService donorService;
    private final BloodBankService bloodBankService;
    private final CertificateService certificateService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Donor>> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        Donor donor = donorService.getDonorByUserId(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", donor));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Donor>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Donor updatedData) {
        Donor donor = donorService.updateProfile(principal.getId(), updatedData);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", donor));
    }

    @PostMapping("/appointments")
    public ResponseEntity<ApiResponse<Appointment>> bookAppointment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AppointmentRequest request) {
        Appointment appointment = donorService.bookAppointment(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Appointment booked successfully", appointment));
    }

    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<List<Appointment>>> getMyAppointments(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<Appointment> appointments = donorService.getMyAppointments(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved", appointments));
    }

    @GetMapping("/donations")
    public ResponseEntity<ApiResponse<List<Donation>>> getDonationHistory(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<Donation> donations = donorService.getDonationHistory(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Donation history retrieved", donations));
    }

    @GetMapping("/donations/{donationId}/certificate")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long donationId) {
        byte[] pdf = certificateService.generateCertificate(donationId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "donation-certificate-" + donationId + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<Notification> notifications = donorService.getMyNotifications(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", notifications));
    }

    @PatchMapping("/notifications/{notificationId}/respond")
    public ResponseEntity<ApiResponse<Notification>> respondToEmergency(
            @PathVariable Long notificationId,
            @RequestParam Notification.DonorResponse response) {
        Notification notification = donorService.respondToEmergency(notificationId, response);
        return ResponseEntity.ok(ApiResponse.success("Response recorded", notification));
    }

    @GetMapping("/blood-banks/nearby")
    public ResponseEntity<ApiResponse<List<BloodBank>>> getNearbyBloodBanks(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "50") double radius) {
        List<BloodBank> banks = bloodBankService.getNearbyBloodBanks(lat, lng, radius);
        return ResponseEntity.ok(ApiResponse.success("Nearby blood banks", banks));
    }
}
