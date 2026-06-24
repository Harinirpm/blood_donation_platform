package com.bloodbridge.controller;

import com.bloodbridge.model.*;
import com.bloodbridge.payload.request.BloodRequestRequest;
import com.bloodbridge.payload.response.ApiResponse;
import com.bloodbridge.security.UserPrincipal;
import com.bloodbridge.service.HospitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospital")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Hospital>> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        Hospital hospital = hospitalService.getHospitalByUserId(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", hospital));
    }

    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<HospitalRequest>> raiseRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BloodRequestRequest request) {
        HospitalRequest req = hospitalService.raiseBloodRequest(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Blood request raised successfully", req));
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<HospitalRequest>>> getMyRequests(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<HospitalRequest> requests = hospitalService.getMyRequests(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Requests retrieved", requests));
    }

    @GetMapping("/requests/{requestId}")
    public ResponseEntity<ApiResponse<HospitalRequest>> getRequest(@PathVariable Long requestId) {
        HospitalRequest request = hospitalService.getRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success("Request retrieved", request));
    }

    @GetMapping("/dispatches")
    public ResponseEntity<ApiResponse<List<Dispatch>>> getDispatches(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<Dispatch> dispatches = hospitalService.getMyDispatches(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Dispatches retrieved", dispatches));
    }

    @PatchMapping("/dispatches/{dispatchId}/confirm")
    public ResponseEntity<ApiResponse<Dispatch>> confirmReceipt(@PathVariable Long dispatchId) {
        Dispatch dispatch = hospitalService.confirmBloodReceipt(dispatchId);
        return ResponseEntity.ok(ApiResponse.success("Blood receipt confirmed", dispatch));
    }

    @GetMapping("/blood-banks/nearby")
    public ResponseEntity<ApiResponse<List<BloodBank>>> getNearbyBloodBanks(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "50") double radius) {
        List<BloodBank> banks = hospitalService.getNearbyBloodBanks(lat, lng, radius);
        return ResponseEntity.ok(ApiResponse.success("Nearby blood banks", banks));
    }
}
