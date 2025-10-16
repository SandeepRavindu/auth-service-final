package com.creatorboost.auth_service.entiy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;


@Entity
@Data
@Table(name = "users")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String userId;
    private String name;
    @Column(unique = true)
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private UserRole role;

    private String imageUrl;
    private String verifyOtp;
    private boolean isAccountVerified;
    private Instant verifyOtpExpiry;
    private  String resetOtp;
    private Instant resetOtpExpiry;

    @CreationTimestamp
    @Column( updatable = false)
    private Instant createdAt;
    @UpdateTimestamp
    private Instant  updatedAt;
    @Column(nullable = false)
    private boolean isSuspended = false;


}

