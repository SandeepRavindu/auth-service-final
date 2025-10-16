package com.creatorboost.auth_service.entiy;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@Table(name = "provider_profiles")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter

public class ProviderProfile {
    @Id
    private Long userId; // Same as in UserEntity
    @OneToOne
    @MapsId // This tells Hibernate to use userId as both PK and FK
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private String title;
    private String location;
    private String memberSince;
    private Double rating;
    private Integer reviewCount;
    private Integer completedOrders;
    private String responseTime;

    @ElementCollection
    private List<String> languages;

    @ElementCollection
    private List<String> skills;

    @Column(length = 2000)
    private String description;

    @ElementCollection
    private List<String> certifications;

    @Column(nullable = false)
    private boolean isApprovedByAdmin = false;

    @Column(nullable = false)
    private boolean isApprovalRequested = false;


}
