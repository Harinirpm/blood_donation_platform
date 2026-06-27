package com.bloodbridge.controller;

import com.bloodbridge.payload.request.*;
import com.bloodbridge.payload.response.ApiResponse;
import com.bloodbridge.payload.response.JwtResponse;
import com.bloodbridge.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {   
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/register/donor")
    public ResponseEntity<ApiResponse<JwtResponse>> registerDonor(@Valid @RequestBody RegisterDonorRequest request) {
        JwtResponse response = authService.registerDonor(request);
        return ResponseEntity.ok(ApiResponse.success("Donor registered successfully", response));
    }

    @PostMapping("/register/blood-bank")
    public ResponseEntity<ApiResponse<JwtResponse>> registerBloodBank(@Valid @RequestBody RegisterBloodBankRequest request) {
        JwtResponse response = authService.registerBloodBank(request);
        return ResponseEntity.ok(ApiResponse.success("Blood bank registered. Awaiting admin approval.", response));
    }

    @PostMapping("/register/hospital")
    public ResponseEntity<ApiResponse<JwtResponse>> registerHospital(@Valid @RequestBody RegisterHospitalRequest request) {
        JwtResponse response = authService.registerHospital(request);
        return ResponseEntity.ok(ApiResponse.success("Hospital registered. Awaiting admin approval.", response));
    }
}
