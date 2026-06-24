package com.bloodbridge.controller;

import com.bloodbridge.model.BloodBank;
import com.bloodbridge.model.BloodGroup;
import com.bloodbridge.model.BloodInventory;
import com.bloodbridge.payload.response.ApiResponse;
import com.bloodbridge.service.BloodBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blood-banks/public")
@RequiredArgsConstructor
public class PublicController {

    private final BloodBankService bloodBankService;

    @GetMapping("/blood-groups")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getBloodGroups() {
        List<Map<String, String>> groups = Arrays.stream(BloodGroup.values())
                .map(bg -> Map.of("value", bg.name(), "label", bg.getDisplay()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Blood groups retrieved", groups));
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<BloodBank>>> getAllBloodBanks() {
        List<BloodBank> banks = bloodBankService.getAllApprovedBloodBanks();
        return ResponseEntity.ok(ApiResponse.success("Blood banks retrieved", banks));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<BloodBank>>> getNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "50") double radius) {
        List<BloodBank> banks = bloodBankService.getNearbyBloodBanks(lat, lng, radius);
        return ResponseEntity.ok(ApiResponse.success("Nearby blood banks", banks));
    }

    @GetMapping("/{bloodBankId}/inventory")
    public ResponseEntity<ApiResponse<List<BloodInventory>>> getInventory(@PathVariable Long bloodBankId) {
        List<BloodInventory> inventory = bloodBankService.getInventory(bloodBankId);
        return ResponseEntity.ok(ApiResponse.success("Inventory retrieved", inventory));
    }
}
