package com.creatorboost.auth_service.io;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileNoteResponse {
    private String providerName;
    private String providerEmail;
    private String note;
    private String fileUrl;
    private String fileType;
    private Instant createdAt;
}
