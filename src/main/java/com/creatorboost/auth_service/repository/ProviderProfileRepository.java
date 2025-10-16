package com.creatorboost.auth_service.repository;

import com.creatorboost.auth_service.entiy.ProviderProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProviderProfileRepository extends JpaRepository<ProviderProfile, String> {
    List<ProviderProfile> findByIsApprovedByAdminFalse();
    List<ProviderProfile> findByIsApprovalRequestedTrueAndIsApprovedByAdminFalse();
}
