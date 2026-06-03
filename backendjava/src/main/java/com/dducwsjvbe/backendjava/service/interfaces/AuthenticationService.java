package com.dducwsjvbe.backendjava.service.interfaces;

import com.dducwsjvbe.backendjava.dto.request.ForgotPassDto;
import com.dducwsjvbe.backendjava.dto.request.LoginRequest;
import com.dducwsjvbe.backendjava.dto.request.SigInRequest;
import com.dducwsjvbe.backendjava.dto.response.TokenResponse;

public interface AuthenticationService {
    TokenResponse login(LoginRequest request) ;
    TokenResponse RefreshToken(String authHeader);

    void register(SigInRequest sigInRequest) ;
    void comfirmusser(Long userId, String secretCode);

    void forgotPassword(ForgotPassDto forgotPassDto) ;
    void comfirmReset(Long userId, String secretCode,String newPassword);
    void logOut (String username) ;
}
