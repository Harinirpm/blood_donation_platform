package com.bloodbridge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EligibilityScheduler {

    private final DonorService donorService;

    // Run every day at midnight to recalculate donor eligibility
    @Scheduled(cron = "0 0 0 * * *")
    public void recalculateDonorEligibility() {
        log.info("Running scheduled donor eligibility recalculation...");
        donorService.recalculateEligibility();
        log.info("Donor eligibility recalculation complete.");
    }
}
