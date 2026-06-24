package com.bloodbridge.payload.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardStats {
    private long totalDonors;
    private long activeDonors;
    private long totalBloodBanks;
    private long approvedBloodBanks;
    private long totalHospitals;
    private long approvedHospitals;
    private long totalRequests;
    private long pendingRequests;
    private long completedRequests;
    private long totalDonations;
    private Map<String, Integer> bloodInventory;
    private Map<String, Long> monthlyDonations;
    private Map<String, Long> requestsByStatus;
}
