package com.creatorboost.auth_service.service;

import com.creatorboost.auth_service.entiy.ClientProfile;
import com.creatorboost.auth_service.entiy.ProfileNote;
import com.creatorboost.auth_service.entiy.ProviderProfile;
import com.creatorboost.auth_service.io.ClientProfileRequset;
import com.creatorboost.auth_service.io.PendingProviderResponse;
import com.creatorboost.auth_service.io.ProfileRequest;
import com.creatorboost.auth_service.io.ProfileResponse;
import com.creatorboost.auth_service.io.ProviderProfileRequest;
import com.creatorboost.auth_service.io.ProfileNoteResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProfileService {
    ProfileResponse createProfile(ProfileRequest request);
    ProfileResponse getProfile(String email);
    void sendResetOtp(String email);
    void resetPassword( String resetOtp, String newPassword);
    void sendOtp(String email);
    void verifyOtp(String email, String otp);
    String getLoggedUserId(String email);

    String uploadFile(MultipartFile file);
    boolean deleteFile(String filename);
    ProfileResponse updateProfileImage(String email, MultipartFile image);

    ProfileResponse updateProviderProfile(ProviderProfileRequest profileData,String email);
    ProfileResponse updateClientProfile(ClientProfileRequset profileData,String email);

    List<ProfileResponse> getAllUsers();

    ProfileResponse getProfileById(String userId);
    void updateUserSuspension(String userId, boolean suspend);

    List<PendingProviderResponse> getPendingProviderApprovals();
    void approveProvider(String userId);
    void requestProviderApproval(String email);
    ProfileNoteResponse createOrUpdateNote(String providerEmail, String note, MultipartFile file);
    ProfileNoteResponse getNoteByProvider(String providerEmail);
}
