package com.bloodbridge.repository;

import com.bloodbridge.model.BloodBank;
import com.bloodbridge.model.BloodGroup;
import com.bloodbridge.model.BloodInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BloodInventoryRepository extends JpaRepository<BloodInventory, Long> {
    List<BloodInventory> findByBloodBank(BloodBank bloodBank);
    Optional<BloodInventory> findByBloodBankAndBloodGroup(BloodBank bloodBank, BloodGroup bloodGroup);
    List<BloodInventory> findByBloodBankBloodBankId(Long bloodBankId);

    @Query("SELECT SUM(i.unitsAvailable) FROM BloodInventory i WHERE i.bloodGroup = :bloodGroup")
    Integer getTotalUnitsByBloodGroup(@Param("bloodGroup") BloodGroup bloodGroup);

    @Query("SELECT SUM(i.unitsAvailable) FROM BloodInventory i WHERE i.bloodBank.bloodBankId = :bankId")
    Integer getTotalUnitsByBloodBank(@Param("bankId") Long bankId);
}
