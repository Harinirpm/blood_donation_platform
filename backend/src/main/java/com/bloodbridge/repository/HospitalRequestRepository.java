package com.bloodbridge.repository;

import com.bloodbridge.model.BloodBank;
import com.bloodbridge.model.Hospital;
import com.bloodbridge.model.HospitalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HospitalRequestRepository extends JpaRepository<HospitalRequest, Long> {
    List<HospitalRequest> findByHospital(Hospital hospital);
    List<HospitalRequest> findByBloodBank(BloodBank bloodBank);
    List<HospitalRequest> findByHospitalOrderByCreatedAtDesc(Hospital hospital);
    List<HospitalRequest> findByBloodBankOrderByCreatedAtDesc(BloodBank bloodBank);
    List<HospitalRequest> findByStatus(HospitalRequest.RequestStatus status);

    @Query("SELECT COUNT(r) FROM HospitalRequest r WHERE r.status = :status")
    long countByStatus(@Param("status") HospitalRequest.RequestStatus status);

    @Query("SELECT COUNT(r) FROM HospitalRequest r")
    long countTotal();

    @Query("""
        SELECT r FROM HospitalRequest r
        WHERE r.bloodBank = :bloodBank
          AND r.status NOT IN ('COMPLETED', 'CANCELLED')
        ORDER BY r.urgencyLevel DESC, r.createdAt ASC
        """)
    List<HospitalRequest> findActiveRequestsByBloodBank(@Param("bloodBank") BloodBank bloodBank);

    @Query("""
        SELECT COUNT(r) FROM HospitalRequest r
        WHERE r.createdAt >= :from AND r.createdAt <= :to
        """)
    long countByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
