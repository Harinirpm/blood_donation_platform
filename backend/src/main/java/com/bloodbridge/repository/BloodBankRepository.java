package com.bloodbridge.repository;

import com.bloodbridge.model.BloodBank;
import com.bloodbridge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BloodBankRepository extends JpaRepository<BloodBank, Long> {
    Optional<BloodBank> findByUser(User user);
    Optional<BloodBank> findByUserUserId(Long userId);
    List<BloodBank> findByApprovalStatus(BloodBank.ApprovalStatus status);
    List<BloodBank> findByCityIgnoreCase(String city);

    @Query("""
        SELECT bb FROM BloodBank bb
        WHERE bb.approvalStatus = 'APPROVED'
          AND bb.latitude IS NOT NULL
          AND bb.longitude IS NOT NULL
          AND (6371 * acos(cos(radians(:lat)) * cos(radians(bb.latitude))
               * cos(radians(bb.longitude) - radians(:lng))
               + sin(radians(:lat)) * sin(radians(bb.latitude)))) <= :radiusKm
        ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(bb.latitude))
               * cos(radians(bb.longitude) - radians(:lng))
               + sin(radians(:lat)) * sin(radians(bb.latitude)))) ASC
        """)
    List<BloodBank> findNearbyBloodBanks(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm);

    long countByApprovalStatus(BloodBank.ApprovalStatus status);
}
