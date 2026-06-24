package com.bloodbridge.controller;

import com.bloodbridge.model.*;
import com.bloodbridge.payload.response.ApiResponse;
import com.bloodbridge.payload.response.DashboardStats;
import com.bloodbridge.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

   
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats", adminService.getDashboardStats()));
    }

    // Listings 
    @GetMapping("/blood-banks")
    public ResponseEntity<ApiResponse<List<BloodBank>>> getAllBloodBanks() {
        return ResponseEntity.ok(ApiResponse.success("Blood banks retrieved", adminService.getAllBloodBanks()));
    }

    @GetMapping("/hospitals")
    public ResponseEntity<ApiResponse<List<Hospital>>> getAllHospitals() {
        return ResponseEntity.ok(ApiResponse.success("Hospitals retrieved", adminService.getAllHospitals()));
    }

    @GetMapping("/donors")
    public ResponseEntity<ApiResponse<List<Donor>>> getAllDonors() {
        return ResponseEntity.ok(ApiResponse.success("Donors retrieved", adminService.getAllDonors()));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", adminService.getAllUsers()));
    }

    @PatchMapping("/users/{userId}/toggle-status")
    public ResponseEntity<ApiResponse<User>> toggleUserStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("User status updated", adminService.toggleUserStatus(userId)));
    }

    //  Pending approvals 
    @GetMapping("/blood-banks/pending")
    public ResponseEntity<ApiResponse<List<BloodBank>>> getPendingBloodBanks() {
        return ResponseEntity.ok(ApiResponse.success("Pending blood banks", adminService.getPendingBloodBanks()));
    }

    @PatchMapping("/blood-banks/{bloodBankId}/approve")
    public ResponseEntity<ApiResponse<BloodBank>> approveBloodBank(
            @PathVariable Long bloodBankId,
            @RequestParam boolean approve) {
        BloodBank bank = adminService.approveBloodBank(bloodBankId, approve);
        String msg = approve ? "Blood bank approved" : "Blood bank rejected";
        return ResponseEntity.ok(ApiResponse.success(msg, bank));
    }

    @GetMapping("/hospitals/pending")
    public ResponseEntity<ApiResponse<List<Hospital>>> getPendingHospitals() {
        return ResponseEntity.ok(ApiResponse.success("Pending hospitals", adminService.getPendingHospitals()));
    }

    @PatchMapping("/hospitals/{hospitalId}/approve")
    public ResponseEntity<ApiResponse<Hospital>> approveHospital(
            @PathVariable Long hospitalId,
            @RequestParam boolean approve) {
        Hospital hospital = adminService.approveHospital(hospitalId, approve);
        String msg = approve ? "Hospital approved" : "Hospital rejected";
        return ResponseEntity.ok(ApiResponse.success(msg, hospital));
    }

    // Request & donation histories 
    @GetMapping("/hospitals/{hospitalId}/requests")
    public ResponseEntity<ApiResponse<List<HospitalRequest>>> getHospitalRequestHistory(
            @PathVariable Long hospitalId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Request history retrieved", adminService.getRequestHistoryForHospital(hospitalId)));
    }

    @GetMapping("/blood-banks/{bloodBankId}/requests")
    public ResponseEntity<ApiResponse<List<HospitalRequest>>> getBloodBankRequestHistory(
            @PathVariable Long bloodBankId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Request history retrieved", adminService.getRequestHistoryForBloodBank(bloodBankId)));
    }

    @GetMapping("/donors/{donorId}/donations")
    public ResponseEntity<ApiResponse<List<Donation>>> getDonorDonationHistory(
            @PathVariable Long donorId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Donation history retrieved", adminService.getDonationHistoryForDonor(donorId)));
    }

    @GetMapping("/blood-banks/{bloodBankId}/donations")
    public ResponseEntity<ApiResponse<List<Donation>>> getBloodBankDonationHistory(
            @PathVariable Long bloodBankId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Donation history retrieved", adminService.getDonationHistoryForBloodBank(bloodBankId)));
    }

    @GetMapping("/hospitals/{hospitalId}/dispatches")
    public ResponseEntity<ApiResponse<List<Dispatch>>> getHospitalDispatchHistory(
            @PathVariable Long hospitalId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Dispatch history retrieved", adminService.getDispatchHistoryForHospital(hospitalId)));
    }

    @GetMapping("/blood-banks/{bloodBankId}/dispatches")
    public ResponseEntity<ApiResponse<List<Dispatch>>> getBloodBankDispatchHistory(
            @PathVariable Long bloodBankId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Dispatch history retrieved", adminService.getDispatchHistoryForBloodBank(bloodBankId)));
    }
}
