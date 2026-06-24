package com.bloodbridge.service;

import com.bloodbridge.exception.BadRequestException;
import com.bloodbridge.model.*;
import com.bloodbridge.payload.request.*;
import com.bloodbridge.payload.response.JwtResponse;
import com.bloodbridge.repository.*;
import com.bloodbridge.security.JwtTokenProvider;
import com.bloodbridge.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DonorRepository donorRepository;
    private final BloodBankRepository bloodBankRepository;
    private final HospitalRepository hospitalRepository;
    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public JwtResponse login(LoginRequest request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        String token = tokenProvider.generateToken(auth);
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        User user = userRepository.findByEmail(principal.getEmail()).orElseThrow();
System.out.println("1. User Role : "+user.getRole().name());
        Long profileId = getProfileId(user);

        return JwtResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .profileId(profileId)
                .build();
    }

    @Transactional
    public JwtResponse registerDonor(RegisterDonorRequest request) {
        validateEmailUnique(request.getEmail());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.DONOR)
                .status(User.UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        Donor donor = Donor.builder()
                .user(user)
                .bloodGroup(request.getBloodGroup())
                .age(request.getAge())
                .gender(request.getGender())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .emergencyContact(request.getEmergencyContact())
                .eligibilityStatus(request.getAge() >= 18)
                .totalDonations(0)
                .build();
        donorRepository.save(donor);

        String token = tokenProvider.generateTokenFromEmail(user.getEmail());
        return JwtResponse.builder()
                .token(token).type("Bearer")
                .userId(user.getUserId()).name(user.getName())
                .email(user.getEmail()).role(user.getRole().name())
                .profileId(donor.getDonorId())
                .build();
    }

    @Transactional
    public JwtResponse registerBloodBank(RegisterBloodBankRequest request) {
        validateEmailUnique(request.getEmail());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.BLOOD_BANK)
                .status(User.UserStatus.PENDING_APPROVAL)
                .build();
        userRepository.save(user);

        BloodBank bloodBank = BloodBank.builder()
                .user(user)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .district(request.getDistrict())
                .state(request.getState())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .workingHours(request.getWorkingHours())
                .description(request.getDescription())
                .approvalStatus(BloodBank.ApprovalStatus.PENDING)
                .build();
        bloodBankRepository.save(bloodBank);

        String token = tokenProvider.generateTokenFromEmail(user.getEmail());
        return JwtResponse.builder()
                .token(token).type("Bearer")
                .userId(user.getUserId()).name(user.getName())
                .email(user.getEmail()).role(user.getRole().name())
                .profileId(bloodBank.getBloodBankId())
                .build();
    }

    @Transactional
    public JwtResponse registerHospital(RegisterHospitalRequest request) {
        validateEmailUnique(request.getEmail());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.HOSPITAL)
                .status(User.UserStatus.PENDING_APPROVAL)
                .build();
        userRepository.save(user);

        Hospital hospital = Hospital.builder()
                .user(user)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .approvalStatus(Hospital.ApprovalStatus.PENDING)
                .build();
        hospitalRepository.save(hospital);

        String token = tokenProvider.generateTokenFromEmail(user.getEmail());
        return JwtResponse.builder()
                .token(token).type("Bearer")
                .userId(user.getUserId()).name(user.getName())
                .email(user.getEmail()).role(user.getRole().name())
                .profileId(hospital.getHospitalId())
                .build();
    }

    private void validateEmailUnique(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already registered: " + email);
        }
    }

    private Long getProfileId(User user) {
        return switch (user.getRole()) {
            case DONOR -> donorRepository.findByUser(user).map(Donor::getDonorId).orElse(null);
            case BLOOD_BANK -> bloodBankRepository.findByUser(user).map(BloodBank::getBloodBankId).orElse(null);
            case HOSPITAL -> hospitalRepository.findByUser(user).map(Hospital::getHospitalId).orElse(null);
            default -> null;
        };
    }
}
