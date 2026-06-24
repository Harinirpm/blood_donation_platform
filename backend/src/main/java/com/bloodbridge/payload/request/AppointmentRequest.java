package com.bloodbridge.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentRequest {
    @NotNull
    private Long bloodBankId;

    @NotNull
    private LocalDate appointmentDate;

    private LocalTime timeSlot;
    private String notes;
}
