package com.bloodbridge.controller;

import com.bloodbridge.model.*;
import com.bloodbridge.payload.request.DonationRequest;
import com.bloodbridge.payload.request.DispatchRequest;
import com.bloodbridge.payload.response.ApiResponse;
import com.bloodbridge.repository.HospitalRequestRepository;
import com.bloodbridge.security.UserPrincipal;
import com.bloodbridge.service.BloodBankService;
import com.bloodbridge.service.DispatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blood-bank")
@RequiredArgsConstructor
public class BloodBankController {

    private final BloodBankService bloodBankService;
    private final DispatchService dispatchService;
    private final HospitalRequestRepository requestRepository;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<BloodBank>> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        BloodBank bank = bloodBankService.getBloodBankByUserId(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", bank));
    }

    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<List<BloodInventory>>> getInventory(
            @AuthenticationPrincipal UserPrincipal principal) {
        BloodBank bank = bloodBankService.getBloodBankByUserId(principal.getId());
        List<BloodInventory> inventory = bloodBankService.getInventory(bank.getBloodBankId());
        return ResponseEntity.ok(ApiResponse.success("Inventory retrieved", inventory));
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<HospitalRequest>>> getRequests(
            @AuthenticationPrincipal UserPrincipal principal) {
        BloodBank bank = bloodBankService.getBloodBankByUserId(principal.getId());
        List<HospitalRequest> requests = requestRepository.findByBloodBankOrderByCreatedAtDesc(bank);
        return ResponseEntity.ok(ApiResponse.success("Requests retrieved", requests));
    }

    @PostMapping("/requests/{requestId}/notify-donors")
    public ResponseEntity<ApiResponse<Void>> sendEmergencyNotifications(
            @PathVariable Long requestId) {
        bloodBankService.sendEmergencyNotifications(requestId);
        return ResponseEntity.ok(ApiResponse.success("Emergency notifications sent to eligible donors"));
    }

    @PatchMapping("/requests/{requestId}/status")
    public ResponseEntity<ApiResponse<HospitalRequest>> updateRequestStatus(
            @PathVariable Long requestId,
            @RequestParam HospitalRequest.RequestStatus status) {
        HospitalRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new com.bloodbridge.exception.ResourceNotFoundException("Request not found"));
        request.setStatus(status);
        requestRepository.save(request);
        return ResponseEntity.ok(ApiResponse.success("Request status updated", request));
    }

    @PostMapping("/donations")
    public ResponseEntity<ApiResponse<Donation>> recordDonation(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DonationRequest request) {
        BloodBank bank = bloodBankService.getBloodBankByUserId(principal.getId());
        Donation donation = bloodBankService.recordDonation(bank.getBloodBankId(), request);
        return ResponseEntity.ok(ApiResponse.success("Donation recorded successfully", donation));
    }

    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<List<Appointment>>> getPendingAppointments(
            @AuthenticationPrincipal UserPrincipal principal) {
        BloodBank bank = bloodBankService.getBloodBankByUserId(principal.getId());
        List<Appointment> appointments = bloodBankService.getPendingAppointments(bank.getBloodBankId());
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved", appointments));
    }

    @PatchMapping("/appointments/{appointmentId}/status")
    public ResponseEntity<ApiResponse<Appointment>> updateAppointment(
            @PathVariable Long appointmentId,
            @RequestParam Appointment.AppointmentStatus status) {
        Appointment appointment = bloodBankService.updateAppointmentStatus(appointmentId, status);
        return ResponseEntity.ok(ApiResponse.success("Appointment updated", appointment));
    }

    @PostMapping("/dispatches")
    public ResponseEntity<ApiResponse<Dispatch>> createDispatch(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DispatchRequest request) {
        BloodBank bank = bloodBankService.getBloodBankByUserId(principal.getId());
        Dispatch dispatch = dispatchService.createDispatch(bank.getBloodBankId(), request);
        return ResponseEntity.ok(ApiResponse.success("Dispatch created", dispatch));
    }

    @GetMapping("/donors/nearby")
    public ResponseEntity<ApiResponse<List<Donor>>> getNearbyDonors(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "50") double radius) {
        List<Donor> donors = bloodBankService.getNearbyDonors(lat, lng, radius);
        return ResponseEntity.ok(ApiResponse.success("Nearby donors", donors));
    }

    @GetMapping("/dispatches")
    public ResponseEntity<ApiResponse<List<Dispatch>>> getDispatches(
            @AuthenticationPrincipal UserPrincipal principal) {
        BloodBank bank = bloodBankService.getBloodBankByUserId(principal.getId());
        List<Dispatch> dispatches = dispatchService.getDispatchesByBloodBank(bank.getBloodBankId());
        return ResponseEntity.ok(ApiResponse.success("Dispatches retrieved", dispatches));
    }
}
