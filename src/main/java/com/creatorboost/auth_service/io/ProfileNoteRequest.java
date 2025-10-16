package com.creatorboost.auth_service.io;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileNoteRequest {
    @NotBlank
    private String note;

    private MultipartFile file; // optional
}
