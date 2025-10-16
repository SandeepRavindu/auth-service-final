package com.creatorboost.auth_service.repository;

import com.creatorboost.auth_service.entiy.ClientProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientProfileRepository extends JpaRepository<ClientProfile, String> {

}
