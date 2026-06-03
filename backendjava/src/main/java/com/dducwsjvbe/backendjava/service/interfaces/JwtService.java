package com.dducwsjvbe.backendjava.service.interfaces;

import com.dducwsjvbe.backendjava.enums.TokenType;
import com.dducwsjvbe.backendjava.model.User;

public interface JwtService {
    String generateAccessToken(User userFromDb);

    String generateRefreshToken(User userFromDb);
    String generateRegisterToken(String username);
    String generateResetPasswordToken(String username);

    String extractUsername(TokenType tokenType, String token) ;

    boolean isTokenValid(TokenType tokenType,String token) ;

}
