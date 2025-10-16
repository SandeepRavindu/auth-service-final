/*package com.creatorboost.auth_service.entiy;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "user_auth_providers")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAuthProvider {
    @Id
    private Long userId; // Same as in UserEntity
    @OneToOne
    @MapsId // This tells Hibernate to use userId as both PK and FK
    @JoinColumn(name = "user_id")
    private UserEntity user;
    private AuthProvider provider;
}*/
