package com.bloodbridge.repository;

import com.bloodbridge.model.BloodBank;
import com.bloodbridge.model.Dispatch;
import com.bloodbridge.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DispatchRepository extends JpaRepository<Dispatch, Long> {
    List<Dispatch> findByBloodBankOrderByCreatedAtDesc(BloodBank bloodBank);
    List<Dispatch> findByHospitalOrderByCreatedAtDesc(Hospital hospital);
    Optional<Dispatch> findByHospitalRequestRequestId(Long requestId);
}
