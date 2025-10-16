package com.creatorboost.auth_service.entiy;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Table(name = "profile_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false, unique = true)
    private ProviderProfile provider;

    @Column(columnDefinition = "TEXT")
    private String note;

    private String fileUrl;

    private String fileType;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant createdAt;
}

