package com.bloodbridge.payload.request;

import com.bloodbridge.model.BloodGroup;
import com.bloodbridge.model.Donation;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DonationRequest {
    @NotNull
    private Long donorId;

    @NotNull
    private BloodGroup bloodGroup;

    @NotNull
    private Integer unitsDonated;

    @NotNull
    private LocalDate donationDate;

    private Long hospitalRequestId;
    private Donation.DonationType donationType;
}
