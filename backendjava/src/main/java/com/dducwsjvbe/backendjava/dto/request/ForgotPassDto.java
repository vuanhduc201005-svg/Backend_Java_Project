package com.dducwsjvbe.backendjava.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPassDto implements Serializable {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String newPassword;
}
