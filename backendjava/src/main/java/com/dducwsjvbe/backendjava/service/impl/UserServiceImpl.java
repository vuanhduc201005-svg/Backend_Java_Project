package com.dducwsjvbe.backendjava.service.impl;

import com.dducwsjvbe.backendjava.dto.request.CreateAdminRequest;
import com.dducwsjvbe.backendjava.dto.response.ResponseData;
import com.dducwsjvbe.backendjava.enums.UserStatus;
import com.dducwsjvbe.backendjava.enums.UserType;
import com.dducwsjvbe.backendjava.exception.ConflictException;
import com.dducwsjvbe.backendjava.model.*;
import com.dducwsjvbe.backendjava.repository.interfaces.*;
import com.dducwsjvbe.backendjava.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@Slf4j(topic = "User-Service-Impl")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserHasRoleRepository userHasRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    //create admin
    @Override
    public ResponseData<?> createAdmin(CreateAdminRequest createAdminRequest) {
        log.info("createAdmin request,username and permission={},{}",createAdminRequest.getUsername(),createAdminRequest.getPermission());
        if (userRepository.existsByUsernameOrEmail(createAdminRequest.getUsername(), createAdminRequest.getEmail())) {
            throw new ConflictException("user and email already exist");
        }
        User user = userRepository.save(User.builder()
                .username(createAdminRequest.getUsername() + "ADMIN" + createAdminRequest.getPermission())
                .password(passwordEncoder.encode(createAdminRequest.getPassword()))
                .email(createAdminRequest.getEmail())
                .phone(createAdminRequest.getPhone())
                .type(UserType.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());
        Role role;
        if (createAdminRequest.getPermission().equals("POST")) {
            role= roleRepository.findById(2).orElseThrow(() -> new RuntimeException("Role Not Found"));
        } else {
            role= roleRepository.findById(4).orElseThrow(() -> new RuntimeException("Role Not Found"));
        }
        userHasRoleRepository.save(UserHasRole.builder()
                .user(user)
                .role(role)
                .build()
        );
        return new ResponseData<>(
                HttpStatus.CREATED.value(),
                "create admin successfuly",
                user.getId()
        );
    }
}
