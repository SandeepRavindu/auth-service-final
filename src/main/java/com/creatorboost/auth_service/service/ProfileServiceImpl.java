package com.creatorboost.auth_service.service;

import com.creatorboost.auth_service.entiy.ClientProfile;
import com.creatorboost.auth_service.entiy.ProfileNote;
import com.creatorboost.auth_service.entiy.ProviderProfile;
import com.creatorboost.auth_service.entiy.UserEntity;
import com.creatorboost.auth_service.io.*;
import com.creatorboost.auth_service.repository.ClientProfileRepository;
import com.creatorboost.auth_service.repository.ProfileNoteRepository;
import com.creatorboost.auth_service.repository.ProviderProfileRepository;
import com.creatorboost.auth_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements   ProfileService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaProducerService kafkaProducerService;
    private final CloudinaryClient cloudinaryClient;
    private final ProviderProfileRepository providerProfileRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final ProfileNoteRepository profileNoteRepository;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ProfileServiceImpl.class);

    @Value("${client.url}")
    private String clientUrl;

    @Override
    public ProfileResponse createProfile(ProfileRequest request){
        UserEntity newProfile= convertToUserEntity(request);
        if (!userRepository.existsByEmail(newProfile.getEmail())) {
            newProfile = userRepository.save(newProfile);

            // Send welcome email via Kafka
            try {
                kafkaProducerService.sendWelcomeEmail(newProfile.getEmail(), newProfile.getName());
                logger.info("✅ Welcome email notification sent for user: {}", newProfile.getEmail());
            } catch (Exception e) {
                logger.error("❌ Failed to send welcome email notification for user: {}", newProfile.getEmail(), e);
                // Don't throw exception here as user creation was successful
            }
            return convertToProfileResponse(newProfile,null,null);
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
    }

    @Override
    @Transactional
    public ProfileResponse getProfile(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email));

        ProviderProfile providerProfile = null;
        ClientProfile clientProfile = null;

        switch (existingUser.getRole()) {
            case PROVIDER -> {
                providerProfile = providerProfileRepository.findById(String.valueOf(existingUser.getId())).orElse(null);
                if (providerProfile != null) {
                    // Force initialization of lazy collections if needed
                }
            }
            case CLIENT -> clientProfile = clientProfileRepository.findById(String.valueOf(existingUser.getId())).orElse(null);
        }
        return convertToProfileResponse(existingUser, providerProfile, clientProfile);
    }

    @Override
    public void sendResetOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email));

        byte[] bytes = new byte[20]; // 20 bytes = 40 hex characters
        new SecureRandom().nextBytes(bytes);
        String resetOtp = HexFormat.of().formatHex(bytes);


        //update the user entity with the reset OTP
        existingUser.setResetOtp(resetOtp);
        existingUser.setResetOtpExpiry(Instant.now().plusSeconds(15 * 60)); // 15 minutes

        // save the updated user entity
        userRepository.save(existingUser);

        try{
            //emailService.sendPasswordResetEmail(existingUser.getEmail(), resetOtp);
            kafkaProducerService.sendPasswordResetOtp(existingUser.getEmail(), clientUrl + "/reset-password/" + resetOtp);
            logger.info("✅ Password reset OTP notification sent for user: {}", existingUser.getEmail());
        }catch(Exception e){
            logger.error("❌ Failed to send password reset OTP notification for user: {}", existingUser.getEmail(), e);
            throw new RuntimeException("unable to send reset OTP", e);
        }
    }

    @Override
    public void resetPassword(String resetOtp, String newPassword) {
        UserEntity existingUser = userRepository.findByResetOtp(resetOtp)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token"));

        if (existingUser.getResetOtp()==null || !existingUser.getResetOtp().equals(resetOtp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reset OTP");
        }

        if (existingUser.getResetOtpExpiry().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset OTP has expired");
        }
        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setResetOtp(null); // Clear the reset OTP after successful reset
        existingUser.setResetOtpExpiry(null); // Clear the expiry time

        userRepository.save(existingUser);

    }

    @Override
    public void sendOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        if( existingUser.isAccountVerified()) {
            return;
        }
        String otp = String.format("%06d", (int) (Math.random() * 1000000));


        existingUser.setVerifyOtp(otp);
        existingUser.setVerifyOtpExpiry(Instant.now().plusSeconds(10 * 60 * 60)); // 10 hours

        userRepository.save(existingUser);

        try {
            //emailService.sendOtpEmail(existingUser.getEmail(), otp);
            kafkaProducerService.sendVerificationOtp(existingUser.getEmail(), otp);
            logger.info("✅ Verification OTP notification sent for user: {}", existingUser.getEmail());
        } catch (Exception e) {
            logger.error("❌ Failed to send verification OTP notification for user: {}", existingUser.getEmail(), e);
            throw new RuntimeException("Unable to send OTP", e);
        }
    }

    @Override
    public void verifyOtp(String email, String otp) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        if (existingUser.getVerifyOtp() == null || !existingUser.getVerifyOtp().equals(otp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }
        if(existingUser.getVerifyOtpExpiry().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP has expired");
        }

        existingUser.setAccountVerified(true);
        existingUser.setVerifyOtp(null); // Clear the OTP after successful verification
        existingUser.setVerifyOtpExpiry(null); // Clear the expiry time
        userRepository.save(existingUser);
    }

    @Override
    public String getLoggedUserId(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return existingUser.getUserId();
    }



    private UserEntity convertToUserEntity(ProfileRequest request) {
       return UserEntity.builder()
               .email(request.getEmail())
               .userId(UUID.randomUUID().toString())
               .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isAccountVerified(false)
               .resetOtpExpiry(null)
               .verifyOtp(null)
               .verifyOtpExpiry(null)
                .resetOtp(null)
                .build();


    }

    private ProfileResponse convertToProfileResponse(UserEntity user, ProviderProfile providerProfile, ClientProfile clientProfile) {
        ProviderProfileResponse providerProfileResponse = null;
        if (providerProfile != null) {
            // Force initialization of lazy collections
            if (providerProfile.getLanguages() != null) providerProfile.getLanguages().size();
            if (providerProfile.getSkills() != null) providerProfile.getSkills().size();
            if (providerProfile.getCertifications() != null) providerProfile.getCertifications().size();
            providerProfileResponse = ProviderProfileResponse.builder()
                    .title(providerProfile.getTitle())
                    .location(providerProfile.getLocation())
                    .description(providerProfile.getDescription())
                    .languages(providerProfile.getLanguages() != null ? List.copyOf(providerProfile.getLanguages()) : null)
                    .skills(providerProfile.getSkills() != null ? List.copyOf(providerProfile.getSkills()) : null)
                    .certifications(providerProfile.getCertifications() != null ? List.copyOf(providerProfile.getCertifications()) : null)
                    .isApprovalRequested(providerProfile.isApprovalRequested())
                    .isApprovedByAdmin(providerProfile.isApprovedByAdmin())
                    .build();
        }
        return ProfileResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .userId(user.getUserId())
                .role(user.getRole())
                .isAccountVerified(user.isAccountVerified())
                .imageUrl(user.getImageUrl())
                .providerProfile(providerProfileResponse)
                .clientProfile(clientProfile)
                .createdAt(user.getCreatedAt())
                .isSuspended(user.isSuspended())
                .build();
    }


    @Override
    public String uploadFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String fileNameExtension = (fileName != null && fileName.contains(".")) ? fileName.substring(fileName.lastIndexOf(".") + 1) : "";
        String key = UUID.randomUUID() + (fileNameExtension.isEmpty() ? "" : "." + fileNameExtension);

        try {
            return cloudinaryClient.uploadFile(file, key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public boolean deleteFile(String filename) {
        try {
            // Assuming CloudinaryClient has a deleteFile method
            return cloudinaryClient.deleteFile(filename);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file: " + filename, e);
        }
    }

    @Override
    public ProfileResponse updateProfileImage(String email, MultipartFile image) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email));

        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
        }

        // Delete old image if exists
        if (existingUser.getImageUrl() != null) {
            try {
                String publicId = extractPublicIdFromUrl(existingUser.getImageUrl());
                deleteFile(publicId);
            } catch (Exception e) {
                logger.error("Failed to delete old image", e);
                // Continue with upload anyway
            }
        }

        // Upload new image
        try {
            String imageUrl = uploadFile(image);

            existingUser.setImageUrl(imageUrl);
            userRepository.save(existingUser);
            return convertToProfileResponse(existingUser,null,null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload profile image", e);
        }
    }

    @Override
    @Transactional
    public ProfileResponse updateProviderProfile(ProviderProfileRequest profileData, String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        ProviderProfile profile = providerProfileRepository.findById(String.valueOf(user.getId()))
                .orElseGet(() -> {

                    ProviderProfile newProfile = new ProviderProfile();
                    newProfile.setUser(user); // Link to UserEntity // or set user directly if using @OneToOne
                    return newProfile;
                });
        // Update profile fields
        profile.setTitle(profileData.getTitle());
        profile.setLocation(profileData.getLocation());
        profile.setLanguages(profileData.getLanguages());
        profile.setSkills(profileData.getSkills());
        profile.setDescription(profileData.getDescription());
        profile.setCertifications(profileData.getCertifications());

        providerProfileRepository.save(profile);
        return convertToProfileResponse(user, profile, null);

    }

    @Override
    @Transactional
    public ProfileResponse updateClientProfile(ClientProfileRequset profileData, String email) {
        logger.info("Updating client profile for email: {}", email);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        logger.debug("Found user with ID: {}", user.getId());
        ClientProfile profile = clientProfileRepository.findById(String.valueOf((user.getId())))
                .orElseGet(() -> {
                    logger.info("ClientProfile not found for user ID: {}. Creating new one.", user.getId());
                    ClientProfile newProfile = new ClientProfile();
                    newProfile.setUser(user); // Link to UserEntity // or set user directly if using @OneToOne
                    return newProfile;
                });
        logger.debug("Updating profile fields. Location: {}, Preferences: {}",
                profileData.getLocation(), profileData.getPreferences());
        profile.setLocation(profileData.getLocation());
        profile.setPreferences(profileData.getPreferences());
        profile.setDescription(profileData.getDescription());

        clientProfileRepository.save(profile);
        return convertToProfileResponse(user, null, profile);

    }

    @Override
    public List<ProfileResponse> getAllUsers() {
        List<UserEntity> users = userRepository.findAll();
        return users.stream()
                .map(user -> convertToProfileResponse(user, null, null))
                .toList();
    }

    @Override
    @Transactional
    public ProfileResponse getProfileById(String userId) {
        UserEntity existingUser = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId));

        ProviderProfile providerProfile = null;
        ClientProfile clientProfile = null;

        switch (existingUser.getRole()) {
            case PROVIDER -> {

                providerProfile = providerProfileRepository.findById(String.valueOf(existingUser.getId())).orElse(null);
                if (providerProfile != null) {
                    // Force initialization of lazy collections if needed
                }
            }
            case CLIENT -> clientProfile = clientProfileRepository.findById(String.valueOf(existingUser.getId())).orElse(null);
        }

        return convertToProfileResponse(existingUser, providerProfile, clientProfile);

    }




    private String extractPublicIdFromUrl(String url) {
        try {
            // Example URL: https://res.cloudinary.com/your-cloud-name/image/upload/v1234567890/abc123.jpg
            String[] parts = url.split("/");
            String filenameWithExt = parts[parts.length - 1]; // abc123.jpg
            return filenameWithExt.substring(0, filenameWithExt.lastIndexOf('.')); // abc123
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract public ID from image URL", e);
        }
    }

    @Override
    @Transactional
    public void updateUserSuspension(String userId, boolean suspend) {
        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setSuspended(suspend);
        userRepository.save(user);

    }

    @Override
    @Transactional
    public List<PendingProviderResponse> getPendingProviderApprovals() {
        List<ProviderProfile> profiles = providerProfileRepository.findByIsApprovalRequestedTrueAndIsApprovedByAdminFalse();
        return profiles.stream().map(profile -> {
            Optional<ProfileNote> noteOpt = profileNoteRepository.findByProvider(profile);
            String noteText = noteOpt.map(ProfileNote::getNote).orElse(null);
            String fileUrl = noteOpt.map(ProfileNote::getFileUrl).orElse(null);
            String fileType = noteOpt.map(ProfileNote::getFileType).orElse(null);

            return new PendingProviderResponse(
                    profile.getUser().getUserId(),
                    profile.getUser().getName(),
                    profile.getUser().getEmail(),
                    profile.getUser().getImageUrl(),
                    profile.isApprovalRequested(),
                    profile.isApprovedByAdmin(),
                    profile.getTitle(),
                    profile.getLocation(),
                    profile.getDescription(),
                    noteText,
                    fileUrl,
                    fileType
            );
        }).toList();
    }

    @Override
    @Transactional
    public void approveProvider(String userId) {
        // Find the user by userId (UUID string)
        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Find the corresponding provider profile
        ProviderProfile profile = providerProfileRepository.findById(String.valueOf(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider profile not found"));

        // Update the flags
        profile.setApprovedByAdmin(true);
        profile.setApprovalRequested(false);

        // Save the updated profile
        providerProfileRepository.save(profile);

        // Optional: log or send Kafka notification
        logger.info("✅ Provider approved by admin: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void requestProviderApproval(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        ProviderProfile profile = providerProfileRepository.findById(String.valueOf(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider profile not found"));
        profile.setApprovalRequested(true);
        providerProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public ProfileNoteResponse createOrUpdateNote(String providerEmail, String note, MultipartFile file) {
        // Find provider profile by email
        UserEntity user = userRepository.findByEmail(providerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() != null && user.getRole().name().equals("PROVIDER")) {
            ProviderProfile providerProfile = providerProfileRepository.findById(String.valueOf(user.getId()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider profile not found"));

            // 4. Check if a note already exists for this provider
            ProfileNote profileNote = profileNoteRepository.findByProvider(providerProfile)
                    .orElse(ProfileNote.builder().provider(providerProfile).build());

            profileNote.setNote(note);

            // Handle file upload if file is present
            if (file != null && !file.isEmpty()) {
                String fileUrl = uploadFile(file);
                profileNote.setFileUrl(fileUrl);
                String fileType = file.getContentType();
                profileNote.setFileType(fileType);
            }

            // Save note
            ProfileNote savedNote = profileNoteRepository.save(profileNote);

            return ProfileNoteResponse.builder()
                    .providerName(savedNote.getProvider().getUser().getName())
                    .providerEmail(savedNote.getProvider().getUser().getEmail())
                    .note(savedNote.getNote())
                    .fileUrl(savedNote.getFileUrl())
                    .fileType(savedNote.getFileType())
                    .createdAt(savedNote.getCreatedAt())
                    .build();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only providers can create notes");
        }
    }

    @Override
    @Transactional
    public ProfileNoteResponse getNoteByProvider(String providerEmail) {
        UserEntity user = userRepository.findByEmail(providerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() != null && user.getRole().name().equals("PROVIDER")) {
            ProviderProfile providerProfile = providerProfileRepository.findById(String.valueOf(user.getId()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider profile not found"));
            ProfileNote note = profileNoteRepository.findByProvider(providerProfile)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile note not found"));

            return ProfileNoteResponse.builder()
                    .providerName(note.getProvider().getUser().getName())
                    .providerEmail(note.getProvider().getUser().getEmail())
                    .note(note.getNote())
                    .fileUrl(note.getFileUrl())
                    .fileType(note.getFileType())
                    .createdAt(note.getCreatedAt())
                    .build();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only providers have notes");
        }
    }


}
