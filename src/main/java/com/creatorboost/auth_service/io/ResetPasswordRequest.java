package com.creatorboost.auth_service.io;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    @NotBlank(message = "Reset OTP is required")
    private String resetOtp;
    @NotBlank(message = "New password cannot be blank")
    private String newPassword;

}
