package com.bloodbridge.service;

import com.bloodbridge.exception.BadRequestException;
import com.bloodbridge.exception.ResourceNotFoundException;
import com.bloodbridge.model.*;
import com.bloodbridge.payload.request.BloodRequestRequest;
import com.bloodbridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final HospitalRequestRepository requestRepository;
    private final BloodBankRepository bloodBankRepository;
    private final DispatchRepository dispatchRepository;

    public Hospital getHospitalByUserId(Long userId) {
        return hospitalRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found for user: " + userId));
    }

    @Transactional
    public HospitalRequest raiseBloodRequest(Long userId, BloodRequestRequest req) {
        Hospital hospital = getHospitalByUserId(userId);

        if (hospital.getApprovalStatus() != Hospital.ApprovalStatus.APPROVED) {
            throw new BadRequestException("Hospital is not approved to raise blood requests.");
        }

        BloodBank bloodBank = bloodBankRepository.findById(req.getBloodBankId())
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found"));

        HospitalRequest request = HospitalRequest.builder()
                .hospital(hospital)
                .bloodBank(bloodBank)
                .bloodGroup(req.getBloodGroup())
                .requiredUnits(req.getRequiredUnits())
                .collectedUnits(0)
                .urgencyLevel(req.getUrgencyLevel())
                .requiredDate(req.getRequiredDate())
                .status(HospitalRequest.RequestStatus.PENDING)
                .notes(req.getNotes())
                .build();

        return requestRepository.save(request);
    }

    public List<HospitalRequest> getMyRequests(Long userId) {
        Hospital hospital = getHospitalByUserId(userId);
        return requestRepository.findByHospitalOrderByCreatedAtDesc(hospital);
    }

    public HospitalRequest getRequest(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + requestId));
    }

    @Transactional
    public Dispatch confirmBloodReceipt(Long dispatchId) {
        Dispatch dispatch = dispatchRepository.findById(dispatchId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch not found"));

        dispatch.setReceivedStatus(Dispatch.ReceivedStatus.CONFIRMED);
        dispatch.setReceivedAt(LocalDateTime.now());

        HospitalRequest request = dispatch.getHospitalRequest();
        request.setStatus(HospitalRequest.RequestStatus.COMPLETED);
        request.setCompletedAt(LocalDateTime.now());

        return dispatchRepository.save(dispatch);
    }

    public List<Dispatch> getMyDispatches(Long userId) {
        Hospital hospital = getHospitalByUserId(userId);
        return dispatchRepository.findByHospitalOrderByCreatedAtDesc(hospital);
    }

    public List<BloodBank> getNearbyBloodBanks(double lat, double lng, double radiusKm) {
        return bloodBankRepository.findNearbyBloodBanks(lat, lng, radiusKm);
    }
}
