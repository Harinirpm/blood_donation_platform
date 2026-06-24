package com.bloodbridge.repository;

import com.bloodbridge.model.BloodBank;
import com.bloodbridge.model.BloodGroup;
import com.bloodbridge.model.Donation;
import com.bloodbridge.model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByDonorOrderByDonationDateDesc(Donor donor);
    List<Donation> findByBloodBankBloodBankId(Long bloodBankId);
    List<Donation> findByBloodBankOrderByDonationDateDesc(BloodBank bloodBank);

    @Query("SELECT COUNT(d) FROM Donation d")
    long countTotal();

    @Query("""
        SELECT d.bloodGroup, SUM(d.unitsDonated) FROM Donation d
        WHERE d.donationDate >= :from
        GROUP BY d.bloodGroup
        """)
    List<Object[]> getDonationsByBloodGroupSince(@Param("from") LocalDate from);

    @Query("""
        SELECT MONTH(d.donationDate), COUNT(d) FROM Donation d
        WHERE YEAR(d.donationDate) = :year
        GROUP BY MONTH(d.donationDate)
        """)
    List<Object[]> getMonthlyDonationStats(@Param("year") int year);
}
