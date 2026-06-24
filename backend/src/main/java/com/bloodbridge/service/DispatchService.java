package com.bloodbridge.service;

import com.bloodbridge.exception.BadRequestException;
import com.bloodbridge.exception.ResourceNotFoundException;
import com.bloodbridge.model.*;
import com.bloodbridge.payload.request.DispatchRequest;
import com.bloodbridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DispatchService {

    private final DispatchRepository dispatchRepository;
    private final HospitalRequestRepository requestRepository;
    private final BloodBankRepository bloodBankRepository;
    private final BloodBankService bloodBankService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Dispatch createDispatch(Long bloodBankId, DispatchRequest req) {
        BloodBank bank = bloodBankRepository.findById(bloodBankId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found"));

        HospitalRequest request = requestRepository.findById(req.getRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (!request.getBloodBank().getBloodBankId().equals(bloodBankId)) {
            throw new BadRequestException("This request does not belong to your blood bank.");
        }

        if (request.getStatus() == HospitalRequest.RequestStatus.DISPATCHED ||
            request.getStatus() == HospitalRequest.RequestStatus.COMPLETED) {
            throw new BadRequestException("Request has already been dispatched.");
        }

        // Deduct from inventory
        bloodBankService.updateInventory(bloodBankId, request.getBloodGroup(), -req.getUnitsSent());

        Dispatch dispatch = Dispatch.builder()
                .hospitalRequest(request)
                .bloodBank(bank)
                .hospital(request.getHospital())
                .bloodGroup(request.getBloodGroup())
                .unitsSent(req.getUnitsSent())
                .dispatchDate(req.getDispatchDate())
                .dispatchTime(req.getDispatchTime())
                .receivedStatus(Dispatch.ReceivedStatus.IN_TRANSIT)
                .trackingNotes(req.getTrackingNotes())
                .build();

        dispatchRepository.save(dispatch);

        // Update request status
        request.setStatus(HospitalRequest.RequestStatus.DISPATCHED);
        requestRepository.save(request);

        // Notify hospital via WebSocket (only if user is accessible)
        try {
            messagingTemplate.convertAndSendToUser(
                    request.getHospital().getUser().getEmail(),
                    "/queue/notifications",
                    "Blood dispatched: " + req.getUnitsSent() + " units of " + request.getBloodGroup().getDisplay()
            );
        } catch (Exception ignored) {
            // WebSocket notification is best-effort; don't fail the dispatch
        }

        return dispatch;
    }

    public List<Dispatch> getDispatchesByBloodBank(Long bloodBankId) {
        BloodBank bank = bloodBankRepository.findById(bloodBankId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found"));
        return dispatchRepository.findByBloodBankOrderByCreatedAtDesc(bank);
    }
}
