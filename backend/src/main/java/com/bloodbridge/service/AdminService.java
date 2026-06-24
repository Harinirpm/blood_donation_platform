package com.bloodbridge.service;

import com.bloodbridge.exception.ResourceNotFoundException;
import com.bloodbridge.model.*;
import com.bloodbridge.payload.response.DashboardStats;
import com.bloodbridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final BloodBankRepository bloodBankRepository;
    private final HospitalRepository hospitalRepository;
    private final DonorRepository donorRepository;
    private final DonationRepository donationRepository;
    private final HospitalRequestRepository requestRepository;
    private final BloodInventoryRepository inventoryRepository;
    private final DispatchRepository dispatchRepository;

    public DashboardStats getDashboardStats() {
        List<BloodInventory> allInventory = inventoryRepository.findAll();
        java.util.Map<String, Integer> inventoryMap = new java.util.HashMap<>();
        for (BloodInventory inv : allInventory) {
            inventoryMap.merge(inv.getBloodGroup().getDisplay(), inv.getUnitsAvailable(), Integer::sum);
        }

        return DashboardStats.builder()
                .totalDonors(userRepository.countByRole(User.Role.DONOR))
                .activeDonors(userRepository.countByRoleAndStatus(User.Role.DONOR, User.UserStatus.ACTIVE))
                .totalBloodBanks(userRepository.countByRole(User.Role.BLOOD_BANK))
                .approvedBloodBanks(bloodBankRepository.countByApprovalStatus(BloodBank.ApprovalStatus.APPROVED))
                .totalHospitals(userRepository.countByRole(User.Role.HOSPITAL))
                .approvedHospitals(hospitalRepository.countByApprovalStatus(Hospital.ApprovalStatus.APPROVED))
                .totalRequests(requestRepository.countTotal())
                .pendingRequests(requestRepository.countByStatus(HospitalRequest.RequestStatus.PENDING))
                .completedRequests(requestRepository.countByStatus(HospitalRequest.RequestStatus.COMPLETED))
                .totalDonations(donationRepository.countTotal())
                .bloodInventory(inventoryMap)
                .build();
    }

    // ── Listings ────────────────────────────────────────────────────────────

    public List<BloodBank> getAllBloodBanks() {
        return bloodBankRepository.findAll();
    }

    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }

    public List<Donor> getAllDonors() {
        return donorRepository.findAll();
    }

    // ── Pending approvals ───────────────────────────────────────────────────

    public List<BloodBank> getPendingBloodBanks() {
        return bloodBankRepository.findByApprovalStatus(BloodBank.ApprovalStatus.PENDING);
    }

    public List<Hospital> getPendingHospitals() {
        return hospitalRepository.findByApprovalStatus(Hospital.ApprovalStatus.PENDING);
    }

    // ── Request histories (admin view) ──────────────────────────────────────

    public List<HospitalRequest> getRequestHistoryForHospital(Long hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));
        return requestRepository.findByHospitalOrderByCreatedAtDesc(hospital);
    }

    public List<HospitalRequest> getRequestHistoryForBloodBank(Long bloodBankId) {
        BloodBank bank = bloodBankRepository.findById(bloodBankId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found"));
        return requestRepository.findByBloodBankOrderByCreatedAtDesc(bank);
    }

    public List<Donation> getDonationHistoryForDonor(Long donorId) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found"));
        return donationRepository.findByDonorOrderByDonationDateDesc(donor);
    }

    public List<Donation> getDonationHistoryForBloodBank(Long bloodBankId) {
        BloodBank bank = bloodBankRepository.findById(bloodBankId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found"));
        return donationRepository.findByBloodBankOrderByDonationDateDesc(bank);
    }

    public List<Dispatch> getDispatchHistoryForHospital(Long hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));
        return dispatchRepository.findByHospitalOrderByCreatedAtDesc(hospital);
    }

    public List<Dispatch> getDispatchHistoryForBloodBank(Long bloodBankId) {
        BloodBank bank = bloodBankRepository.findById(bloodBankId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found"));
        return dispatchRepository.findByBloodBankOrderByCreatedAtDesc(bank);
    }

    // ── Approvals ───────────────────────────────────────────────────────────

    @Transactional
    public BloodBank approveBloodBank(Long bloodBankId, boolean approve) {
        BloodBank bank = bloodBankRepository.findById(bloodBankId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found"));
        bank.setApprovalStatus(approve ? BloodBank.ApprovalStatus.APPROVED : BloodBank.ApprovalStatus.REJECTED);
        User user = bank.getUser();
        user.setStatus(approve ? User.UserStatus.ACTIVE : User.UserStatus.INACTIVE);
        userRepository.save(user);
        return bloodBankRepository.save(bank);
    }

    @Transactional
    public Hospital approveHospital(Long hospitalId, boolean approve) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));
        hospital.setApprovalStatus(approve ? Hospital.ApprovalStatus.APPROVED : Hospital.ApprovalStatus.REJECTED);
        User user = hospital.getUser();
        user.setStatus(approve ? User.UserStatus.ACTIVE : User.UserStatus.INACTIVE);
        userRepository.save(user);
        return hospitalRepository.save(hospital);
    }

    // ── Users ───────────────────────────────────────────────────────────────

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(user.getStatus() == User.UserStatus.ACTIVE ?
                User.UserStatus.INACTIVE : User.UserStatus.ACTIVE);
        return userRepository.save(user);
    }
}
