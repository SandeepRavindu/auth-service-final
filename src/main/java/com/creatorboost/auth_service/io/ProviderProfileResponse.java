package com.creatorboost.auth_service.io;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderProfileResponse {
    private String title;
    private String location;
    private String description;
    private List<String> languages;
    private List<String> skills;
    private List<String> certifications;
    private boolean isApprovalRequested;
    private boolean isApprovedByAdmin;
}

