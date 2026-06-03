package com.dducwsjvbe.backendjava.dto.request;

import com.dducwsjvbe.backendjava.annotation.interfaces.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.io.Serializable;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateAdminRequest implements Serializable {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @Email
    @NotBlank
    private String email;
    @PhoneNumber
    private String phone;
    @Pattern(regexp = "^(POST|UPDATE)$")
    private String permission;
}
