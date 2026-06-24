package com.bloodbridge.payload.request;

import com.bloodbridge.model.BloodGroup;
import com.bloodbridge.model.Donor;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterDonorRequest {
    @NotBlank
    private String name;

    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 6)
    private String password;

    @NotNull @Min(18)
    private Integer age;

    private Donor.Gender gender;

    @NotNull
    private BloodGroup bloodGroup;

    @NotBlank
    private String phone;

    private String address;
    private String city;
    private Double latitude;
    private Double longitude;
    private String emergencyContact;
}
