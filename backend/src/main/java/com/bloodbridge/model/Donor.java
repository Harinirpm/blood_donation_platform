package com.bloodbridge.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "donors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long donorId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodGroup bloodGroup;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String phone;
    private String address;
    private String city;
    private Double latitude;
    private Double longitude;
    private String emergencyContact;
    private LocalDate lastDonationDate;
    private LocalDate nextEligibleDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean eligibilityStatus = true;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalDonations = 0;

    public enum Gender {
        MALE, FEMALE, OTHER
    }
}
