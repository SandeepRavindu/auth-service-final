package com.creatorboost.auth_service.io;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PendingProviderResponse {
    private String userId;
    private String name;
    private String email;
    private String imageUrl;
    private boolean isApprovalRequested;
    private boolean isApprovedByAdmin;
    private String title;
    private String location;
    private String description;
    private String note;
    private String fileUrl;
    private String fileType;
}
