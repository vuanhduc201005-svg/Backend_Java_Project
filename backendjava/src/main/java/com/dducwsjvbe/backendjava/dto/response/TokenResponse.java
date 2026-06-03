package com.dducwsjvbe.backendjava.dto.response;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponse implements Serializable {
    private String accessToken;
    private String refreshToken;
    private String rolePermission;
}
