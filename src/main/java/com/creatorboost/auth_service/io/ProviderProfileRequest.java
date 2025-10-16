package com.creatorboost.auth_service.io;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProviderProfileRequest {
    private String title;
    private String location;
    private String description;
    private List<String> languages;
    private List<String> skills;
    private List<String> certifications;
}
