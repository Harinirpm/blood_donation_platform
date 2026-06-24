package com.bloodbridge.repository;

import com.bloodbridge.model.BloodGroup;
import com.bloodbridge.model.Donor;
import com.bloodbridge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {
    Optional<Donor> findByUser(User user);
    Optional<Donor> findByUserUserId(Long userId);
    List<Donor> findByBloodGroupAndEligibilityStatus(BloodGroup bloodGroup, Boolean eligibilityStatus);

    @Query("""
        SELECT d FROM Donor d
        WHERE d.bloodGroup = :bloodGroup
          AND d.eligibilityStatus = true
          AND d.latitude IS NOT NULL
          AND d.longitude IS NOT NULL
          AND (6371 * acos(cos(radians(:lat)) * cos(radians(d.latitude))
               * cos(radians(d.longitude) - radians(:lng))
               + sin(radians(:lat)) * sin(radians(d.latitude)))) <= :radiusKm
        ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(d.latitude))
               * cos(radians(d.longitude) - radians(:lng))
               + sin(radians(:lat)) * sin(radians(d.latitude)))) ASC
        """)
    List<Donor> findEligibleDonorsByBloodGroupAndLocation(
            @Param("bloodGroup") BloodGroup bloodGroup,
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm);

    @Query("""
        SELECT d FROM Donor d
        WHERE d.latitude IS NOT NULL
          AND d.longitude IS NOT NULL
          AND (6371 * acos(cos(radians(:lat)) * cos(radians(d.latitude))
               * cos(radians(d.longitude) - radians(:lng))
               + sin(radians(:lat)) * sin(radians(d.latitude)))) <= :radiusKm
        ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(d.latitude))
               * cos(radians(d.longitude) - radians(:lng))
               + sin(radians(:lat)) * sin(radians(d.latitude)))) ASC
        """)
    List<Donor> findNearbyDonors(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm);
}
