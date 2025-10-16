package com.creatorboost.auth_service.io;

import com.creatorboost.auth_service.entiy.ClientProfile;
import com.creatorboost.auth_service.entiy.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileResponse {
    private String userId;
    private String name;
    private String email;
    private UserRole role;
    private boolean isAccountVerified;
    private String imageUrl;
    private ProviderProfileResponse providerProfile;
    private ClientProfile clientProfile;

    private Instant createdAt;
    private boolean isSuspended;

}
