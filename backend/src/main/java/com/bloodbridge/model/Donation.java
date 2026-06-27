package com.bloodbridge.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long donationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user"})
    private Donor donor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_bank_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user"})
    private BloodBank bloodBank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "hospital", "bloodBank"})
    private HospitalRequest hospitalRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodGroup bloodGroup;

    @Column(nullable = false)
    private Integer unitsDonated = 1;

    @Column(nullable = false)
    private LocalDate donationDate;

    private LocalDate nextEligibleDate;

    @Enumerated(EnumType.STRING)
    private DonationType donationType;

    private String certificateId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum DonationType {
        VOLUNTARY, EMERGENCY
    }
    
}
