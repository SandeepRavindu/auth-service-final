package com.creatorboost.auth_service.repository;

import com.creatorboost.auth_service.entiy.ProfileNote;
import com.creatorboost.auth_service.entiy.ProviderProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileNoteRepository extends JpaRepository<ProfileNote, Long> {
    Optional<ProfileNote> findByProvider(ProviderProfile provider);
}