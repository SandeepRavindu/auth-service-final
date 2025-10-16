package com.creatorboost.auth_service.service;

import com.creatorboost.auth_service.entiy.ClientProfile;
import com.creatorboost.auth_service.entiy.ProviderProfile;
import com.creatorboost.auth_service.entiy.UserEntity;
import com.creatorboost.auth_service.entiy.UserRole;
import com.creatorboost.auth_service.io.ProfileRequest;
import com.creatorboost.auth_service.io.ProfileResponse;
import com.creatorboost.auth_service.repository.ClientProfileRepository;
import com.creatorboost.auth_service.repository.ProviderProfileRepository;
import com.creatorboost.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private KafkaProducerService kafkaProducerService;
    @Mock
    private CloudinaryClient cloudinaryClient;
    @Mock
    private ProviderProfileRepository providerProfileRepository;
    @Mock
    private ClientProfileRepository clientProfileRepository;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = UserEntity.builder()
                .email("test@example.com")
                .name("Test User")
                .userId(UUID.randomUUID().toString())
                .password("encodedPass")
                .role(UserRole.CLIENT)
                .isAccountVerified(false)
                .build();
    }

    @Test
    void createProfile_ShouldSaveUser_WhenEmailNotExists() {
        ProfileRequest request = new ProfileRequest();
        request.setEmail("new@example.com");
        request.setName("New User");
        request.setPassword("password123");
        request.setRole(UserRole.CLIENT);

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPass");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        ProfileResponse response = profileService.createProfile(request);

        assertEquals("new@example.com", response.getEmail());
        verify(userRepository).save(any(UserEntity.class));
        verify(kafkaProducerService).sendWelcomeEmail(eq("new@example.com"), eq("New User"));
    }

    @Test
    void createProfile_ShouldThrowConflict_WhenEmailExists() {
        ProfileRequest request = new ProfileRequest();
        request.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> profileService.createProfile(request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getProfile_ShouldReturnClientProfile_WhenRoleIsClient() {
        testUser.setRole(UserRole.CLIENT);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(clientProfileRepository.findById(String.valueOf(testUser.getId())))
                .thenReturn(Optional.of(new ClientProfile()));

        ProfileResponse response = profileService.getProfile("test@example.com");

        assertEquals(testUser.getEmail(), response.getEmail());
        verify(clientProfileRepository).findById(String.valueOf(testUser.getId()));
    }

    @Test
    void getProfile_ShouldThrowNotFound_WhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> profileService.getProfile("missing@example.com"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getLoggedUserId_ShouldReturnUserId_WhenUserExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        String userId = profileService.getLoggedUserId("test@example.com");

        assertEquals(testUser.getUserId(), userId);
    }

    @Test
    void getLoggedUserId_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> profileService.getLoggedUserId("missing@example.com"));
    }
}
