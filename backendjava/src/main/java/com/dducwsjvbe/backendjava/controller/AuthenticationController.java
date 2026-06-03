package com.dducwsjvbe.backendjava.controller;

import com.dducwsjvbe.backendjava.dto.request.ForgotPassDto;
import com.dducwsjvbe.backendjava.dto.request.LoginRequest;
import com.dducwsjvbe.backendjava.dto.request.SigInRequest;
import com.dducwsjvbe.backendjava.dto.response.ResponseData;
import com.dducwsjvbe.backendjava.dto.response.ResponseError;
import com.dducwsjvbe.backendjava.dto.response.TokenResponse;
import com.dducwsjvbe.backendjava.service.interfaces.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
@Slf4j(topic = "Authentication-Controller")
@RequiredArgsConstructor
@Tag(name = "Authentication Controller")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(method = "POST", summary = "login", description = "Logging in to access the service")
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        log.info("Access token request,{},{}",request.getUsername(),request.getPassword());
        return authenticationService.login(request);
    }

    @Operation(method = "POST", summary = "refresh token", description = "Requesting access to the service")
    @PostMapping("/refresh-token")
    public TokenResponse refreshToken(@RequestHeader("Authorization") String authHeader) {
        log.info("Refresh token request");
        return authenticationService.RefreshToken(authHeader);
    }

    @Operation(method = "POST", summary = "register", description = "Create an account to use the service")
    @PostMapping("/register")
    public ResponseData<String> register(@Valid @RequestBody SigInRequest sigInRequest) {
        log.info("Register user request{}", sigInRequest.getUsername());
        authenticationService.register(sigInRequest);
        return new ResponseData<>(
                HttpStatus.NO_CONTENT.value(),
                "please confirm registration via email"
        );
    }

    @Operation(method = "GET", summary = "comfirm account", description = "Confirm your registration via email")
    @GetMapping("/comfirm/{userId}")
    public void comfirmUser(@PathVariable @Min(1) Long userId, @RequestParam String secretCode, HttpServletResponse response) throws RuntimeException, IOException {
        log.info("comfirm user id {}", userId);
        authenticationService.comfirmusser(userId, secretCode);
        response.sendRedirect("https://github.com/vuanhduc201005-svg");

    }

    @Operation(method = "POST", summary = "forgot password", description = "recover password")
    @PostMapping("/forgot-password")
    public ResponseData<String> forgotPassword(@Valid @RequestBody ForgotPassDto forgotPassDto) {
        log.info("Forgot password request,email={}", forgotPassDto.getEmail());
        authenticationService.forgotPassword(forgotPassDto);
        return new ResponseData<>(
                HttpStatus.NO_CONTENT.value(),
                "please confirm your password to email");
    }

    @Operation(method = "GET", summary = "comfirm reset password", description = "Confirm new password")
    @GetMapping("/comfirm-reset-password/{userId}")
    public void comfirmResetPassword(@PathVariable @Min(1) Long userId, @RequestParam String secretCode, @RequestParam String newPassword, HttpServletResponse response) throws RuntimeException, IOException {
        log.info("comfirm reset password request,userId={}", userId);
        authenticationService.comfirmReset(userId, secretCode, newPassword);
        response.sendRedirect("https://github.com/vuanhduc201005-svg");
    }

    @Operation(method = "POST", summary = "logout", description = "Temporarily suspend service access")
    @PostMapping("/logout")
    public void logout(@RequestParam String username) {
        log.info("Logout username {}", username);
        authenticationService.logOut(username);
    }
}
