package com.bloodbridge.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "dispatches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dispatchId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "hospital", "bloodBank"})
    private HospitalRequest hospitalRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_bank_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user"})
    private BloodBank bloodBank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user"})
    private Hospital hospital;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodGroup bloodGroup;

    @Column(nullable = false)
    private Integer unitsSent;

    private LocalDate dispatchDate;
    private LocalTime dispatchTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReceivedStatus receivedStatus = ReceivedStatus.PENDING;

    private LocalDateTime receivedAt;
    private String trackingNotes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum ReceivedStatus {
        PENDING, IN_TRANSIT, RECEIVED, CONFIRMED
    }
}
