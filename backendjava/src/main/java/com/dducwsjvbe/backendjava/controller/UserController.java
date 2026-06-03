package com.dducwsjvbe.backendjava.controller;

import com.dducwsjvbe.backendjava.dto.request.CreateAdminRequest;
import com.dducwsjvbe.backendjava.dto.response.ResponseData;
import com.dducwsjvbe.backendjava.repository.interfaces.UserRepository;
import com.dducwsjvbe.backendjava.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Slf4j(topic = "User-Controller")
@RequiredArgsConstructor
@Tag(name="User-Controller")
public class UserController {
    private final UserService userService;

    //create admin
    @Operation(method = "POST", summary = "create admin", description = "initial management")
    @PostMapping("/create-admin")
    public ResponseData<?> createAdmin(@Valid  @RequestBody CreateAdminRequest createAdminRequest) {
        log.info("create admin request");
        return userService.createAdmin(createAdminRequest);
    }
}
