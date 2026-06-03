package com.dducwsjvbe.backendjava.service.impl;

import com.dducwsjvbe.backendjava.enums.TokenType;
import com.dducwsjvbe.backendjava.exception.JwtAuthenticationException;
import com.dducwsjvbe.backendjava.model.User;
import com.dducwsjvbe.backendjava.service.interfaces.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Service
@Slf4j(topic = "Jwt-Service-Impl")
public class JwtServiceImpl implements JwtService {
    @Value("${jwt.expiryMinutes}")
    private Long expiryMinutes;
    @Value("${jwt.expiryDay}")
    private Long expiryDay;
    @Value("${jwt.accessKey}")
    private String accessKey;
    @Value("${jwt.refreshKey}")
    private String refreshKey;
    @Value("${jwt.registerKey}")
    private String registerKey;
    @Value("${jwt.forgotKey}")
    private String forgotKey;
//1p
    @Override
    public String generateAccessToken(User userFromDb) {
        log.info("generateAccessToken,username={}", userFromDb.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", UUID.randomUUID().toString());
        return Jwts
                .builder()
                .addClaims(claims)
                .setSubject(userFromDb.getUsername())
                .setIssuer("DDLG")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 1000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessKey)))
                .compact();
    }

    @Override
    public String generateRefreshToken(User userFromDb) {
        log.info("generateRefreshToken,userName={}", userFromDb.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", UUID.randomUUID().toString());
        return Jwts
                .builder()
                .addClaims(claims)
                .setSubject(userFromDb.getUsername())
                .setIssuer("DDLG")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 1000 * 60 * 24))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshKey)))
                .compact();

    }

    @Override
    public String generateRegisterToken(String username) {
        log.info("generateRegisterToken,userName={}", username);
        Map<String, Object> claims = new HashMap<>();
        return Jwts
                .builder()
                .addClaims(claims)
                .setSubject(username)
                .setIssuer("DDLG")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 1000 * 60*2))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(registerKey)))
                .compact();
    }

    @Override
    public String generateResetPasswordToken(String username) {
        log.info("generateRegisterToken,userName={}", username);
        Map<String, Object> claims = new HashMap<>();
        return Jwts
                .builder()
                .addClaims(claims)
                .setSubject(username)
                .setIssuer("DDLG")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 1000 * 60))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(forgotKey)))
                .compact();
    }

    @Override
    public String extractUsername(TokenType tokenType,String token)  {
        log.info("extractUsername,tokenType={}", tokenType);
            return extractClaims(tokenType,token, Claims::getSubject);
    }

    private <T> T extractClaims(TokenType tokenType,String token, Function<Claims, T> claimsExtractor)  {
        final Claims claims = extraAllClaims(tokenType,token);
        return claimsExtractor.apply(claims);
    }

    private Claims extraAllClaims(TokenType tokenType,String token) throws JwtAuthenticationException {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(typeToString(tokenType))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (RuntimeException e) {
            throw new JwtAuthenticationException("error:" + e.getMessage());
        }
    }

    @Override
    public boolean isTokenValid(TokenType tokenType,String token)  {
        log.info("isTokenValid,tokenType={}", tokenType);
        return !isTokenExpired(tokenType,token);
    }
    private boolean isTokenExpired(TokenType tokenType,String token)  {
        return extractExpiration(tokenType,token).before(new Date());
    }
    private Date extractExpiration(TokenType tokenType,String token)  {
        //nhận token trả ra time hết hạn
        return extractClaims(tokenType,token, Claims::getExpiration);
    }

    private SecretKey typeToString(TokenType tokenType) throws JwtAuthenticationException {
        switch (tokenType) {
            case ACCESS_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessKey));
            }
            case REFRESH_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshKey));
            }
            case REGISTER_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(registerKey));
            }
            case FORGOT_PASSWORD_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(forgotKey));
            }
        }
        throw new JwtAuthenticationException("tokenType not supported");
    }

}
