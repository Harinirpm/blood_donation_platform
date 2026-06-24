package com.bloodbridge.payload.request;

import com.bloodbridge.model.BloodGroup;
import com.bloodbridge.model.HospitalRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BloodRequestRequest {
    @NotNull
    private Long bloodBankId;

    @NotNull
    private BloodGroup bloodGroup;

    @NotNull @Min(1)
    private Integer requiredUnits;

    @NotNull
    private HospitalRequest.UrgencyLevel urgencyLevel;

    private LocalDateTime requiredDate;
    private String notes;
}
