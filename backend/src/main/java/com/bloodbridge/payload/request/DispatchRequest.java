package com.bloodbridge.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DispatchRequest {
    @NotNull
    private Long requestId;

    @NotNull @Min(1)
    private Integer unitsSent;

    @NotNull
    private LocalDate dispatchDate;

    private LocalTime dispatchTime;
    private String trackingNotes;
}
