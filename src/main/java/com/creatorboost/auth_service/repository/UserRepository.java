package com.creatorboost.auth_service.repository;

import com.creatorboost.auth_service.entiy.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    UserEntity findByVerifyOtp(String verifyOtp);

    boolean existsByEmail(String email);
    Optional<UserEntity> findByResetOtp(String resetOtp);
    Optional<UserEntity> findByUserId(String userId);

}
