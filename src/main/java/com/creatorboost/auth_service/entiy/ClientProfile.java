package com.creatorboost.auth_service.entiy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "client_profiles")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientProfile {
    @Id
    private Long userId; // Same as in UserEntity
    @OneToOne
    @MapsId // This tells Hibernate to use userId as both PK and FK
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private String preferences;
    private String location;
    @Column(length = 2000)
    private String description;


    // add more client-specific fields
}
