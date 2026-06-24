package com.bloodbridge.repository;

import com.bloodbridge.model.Hospital;
import com.bloodbridge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    Optional<Hospital> findByUser(User user);
    Optional<Hospital> findByUserUserId(Long userId);
    List<Hospital> findByApprovalStatus(Hospital.ApprovalStatus status);
    long countByApprovalStatus(Hospital.ApprovalStatus status);

    @Query("""
        SELECT h FROM Hospital h
        WHERE h.approvalStatus = 'APPROVED'
          AND h.latitude IS NOT NULL
          AND h.longitude IS NOT NULL
          AND (6371 * acos(cos(radians(:lat)) * cos(radians(h.latitude))
               * cos(radians(h.longitude) - radians(:lng))
               + sin(radians(:lat)) * sin(radians(h.latitude)))) <= :radiusKm
        ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(h.latitude))
               * cos(radians(h.longitude) - radians(:lng))
               + sin(radians(:lat)) * sin(radians(h.latitude)))) ASC
        """)
    List<Hospital> findNearbyHospitals(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm);
}
