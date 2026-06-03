package com.dducwsjvbe.backendjava.service.impl;

import com.dducwsjvbe.backendjava.dto.request.ForgotPassDto;
import com.dducwsjvbe.backendjava.dto.request.LoginRequest;
import com.dducwsjvbe.backendjava.dto.request.SigInRequest;
import com.dducwsjvbe.backendjava.dto.response.TokenResponse;
import com.dducwsjvbe.backendjava.enums.TokenType;
import com.dducwsjvbe.backendjava.enums.UserStatus;
import com.dducwsjvbe.backendjava.enums.UserType;
import com.dducwsjvbe.backendjava.exception.*;
import com.dducwsjvbe.backendjava.model.*;
import com.dducwsjvbe.backendjava.repository.interfaces.*;
import com.dducwsjvbe.backendjava.service.auth.CustomUserDetails;
import com.dducwsjvbe.backendjava.service.interfaces.AuthenticationService;
import com.dducwsjvbe.backendjava.service.interfaces.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Slf4j(topic = "Authentication-Service-Impl")
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleHasPermissionRepository roleHasPermissionRepository;
    private final BlackListTokenRefreshRepository blackListTokenRefreshRepository;
    private final UserHasRoleRepository userHasRoleRepository;
    private final AuthenticationManager authenticationManager;
    private final UserUncomfirmRepository userUncomfirmRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${system.username}")
    private String usernameSystem;
    @Value("${system.password}")
    private String passwordSystem;
    @Value("${system.email}")
    private String emailSystem;

    @Override
    public TokenResponse login(LoginRequest request) {
        log.info("In AuthenticationServiceImpl.login={},{}", request.getUsername(),request.getPassword());
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));
        if (authenticate.isAuthenticated()) {
            CustomUserDetails userDetails =
                    (CustomUserDetails) authenticate.getPrincipal();
            User userFromDb = userDetails.getUser();
            log.info("name={}",userFromDb.getUsername());
            String accessToken = jwtService.generateAccessToken(userFromDb);
            //lần đầu login||token hết hạn
            String refreshToken;
            try {
                if (userFromDb.getTokenRefresh() == null || !jwtService.isTokenValid(TokenType.REFRESH_TOKEN, userFromDb.getTokenRefresh())) {
                    refreshToken = jwtService.generateRefreshToken(userFromDb);
                    userFromDb.setTokenRefresh(refreshToken);
                } else {
                    refreshToken = userFromDb.getTokenRefresh();
                }
            } catch (RuntimeException e) {
                refreshToken = jwtService.generateRefreshToken(userFromDb);
                userFromDb.setTokenRefresh(refreshToken);
            }

            userFromDb.setTokenRefreshStatus(UserStatus.ACTIVE);
            userRepository.save(userFromDb);
            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .rolePermission(userDetails.getAuthorities().toString())
                    .build();
        }
        throw new UsernameNotFoundException("Invalid username or password");
    }

    @Override
    public TokenResponse RefreshToken(String authHeader) {
        log.info("In AuthenticationServiceImpl.getRefreshToken");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new JwtAuthenticationException("Invalid token");
        }
        final String token = authHeader.substring(7);
        String username;
        try {
            username = jwtService.extractUsername(TokenType.REFRESH_TOKEN, token);
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Token expired");
        } catch (Exception e) {
            throw new JwtAuthenticationException("Invalid token");
        }
        final User userFromDb = userRepository.findByUsername(username);
        if (userFromDb == null) {
            throw new JwtAuthenticationException(" token invalid");
        }
        if (userFromDb.getBlackListTokenRefresh() != null && userFromDb.getBlackListTokenRefresh().getTokenBlack() != null) {
            if (userFromDb.getBlackListTokenRefresh().getTokenBlack().equals(token)) {
                throw new JwtAuthenticationException("Invalid token");
            }
        }

        if (!jwtService.isTokenValid(TokenType.REFRESH_TOKEN, token)) {
            throw new TokenExpiredException("token expired");
        }
        if (userFromDb.getTokenRefreshStatus() == UserStatus.INACTIVE) {
            throw new JwtAuthenticationException("Invalid token");
        }
        return TokenResponse.builder()
                .accessToken(jwtService.generateAccessToken(userFromDb))
                .build();
    }

    @Override
    @Transactional
    public void register(SigInRequest sigInRequest) {
        log.info("In AuthenticationServiceImpl.register={},{},{}", sigInRequest.getUsername(), sigInRequest.getPassword(),sigInRequest.getEmail());
        if (userRepository.findById(1L).isEmpty()) {
            createSystem();
        }
        if (userRepository.existsByUsernameOrEmail(sigInRequest.getUsername(), sigInRequest.getEmail())) {
            throw new ConflictException("user and email already exist");
        }
        UserUncomfirm userUncomfirm = userUncomfirmRepository.findByUserName(sigInRequest.getUsername());
        if (userUncomfirm == null) {
            UserUncomfirm save = userUncomfirmRepository.save(UserUncomfirm.builder()
                    .userName(sigInRequest.getUsername())
                    .email(sigInRequest.getEmail())
                    .password(sigInRequest.getPassword())
                    .tokenRegister(jwtService.generateRegisterToken(sigInRequest.getUsername()))
                    .build());
            if (save.getId() != null) {
                /*
                khi .save jpa lưu id vào memory đôi khi chưa comit mà kafka đã gửi
                =>comit xong mới gửi
                 */
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        log.info("secretCode={}", save.getTokenRegister());
                        String message = String.format("email=%s,id=%s,secretCode=%s", save.getEmail(), save.getId(), save.getTokenRegister());
                        kafkaTemplate.send("comfirm-account-topic", message);
                    }
                });
            }
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("secretCode={}", userUncomfirm.getTokenRegister());
                String message = String.format("email=%s,id=%s,secretCode=%s", userUncomfirm.getEmail(), userUncomfirm.getId(), userUncomfirm.getTokenRegister());
                kafkaTemplate.send("comfirm-account-topic", message);
            }
        });
    }

    @Override
    @Transactional
    public void comfirmusser(Long userId, String secretCode) {
        log.info("In AuthenticationServiceImpl.comfirmusser={}",userId);
        UserUncomfirm uncomfirm = userUncomfirmRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        String username;
        try {
            //extrac nếu token hết hạn catch bắt ngay
            username = jwtService.extractUsername(TokenType.REGISTER_TOKEN, secretCode);
        } catch (ExpiredJwtException e) {
            uncomfirm.setTokenRegister(jwtService.generateRegisterToken(uncomfirm.getUserName()));
            userUncomfirmRepository.save(uncomfirm);
            //save ok chưa chắc commit
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    String message = String.format("email=%s,id=%s,secretCode=%s", uncomfirm.getEmail(), uncomfirm.getId(), uncomfirm.getTokenRegister());
                    kafkaTemplate.send("comfirm-account-topic",message);
                }
            });
            return;
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid token");
        }
        if (!uncomfirm.getUserName().equals(username)) {
            throw new InvalidTokenException("Invalid token");
        }
        if (!jwtService.isTokenValid(TokenType.REGISTER_TOKEN, secretCode)) {
            throw new TokenExpiredException("token expired");
        }
        if (userRepository.existsByUsernameOrEmail(username, uncomfirm.getEmail())) {
            throw new ConflictException("username or email already exist");
        }
        User saveUser = userRepository.save(
                User.builder()
                        .username(uncomfirm.getUserName())
                        .email(uncomfirm.getEmail())
                        .password(passwordEncoder.encode(uncomfirm.getPassword()))
                        .status(UserStatus.ACTIVE)
                        .build()
        );
        userHasRoleRepository.save(
                UserHasRole.builder()
                        .user(saveUser)
                        .role(roleRepository.findById(3).orElseThrow(() -> new RuntimeException("role not found")))
                        .build()
        );
        userUncomfirmRepository.deleteById(uncomfirm.getId());
    }

    @Override
    public void forgotPassword(ForgotPassDto forgotPassDto) {
        log.info("In AuthenticationServiceImpl.forgotPassword={}", forgotPassDto.getEmail());
        User user = userRepository.findByEmail(forgotPassDto.getEmail());
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        String message = String.format("email=%s,id=%s,newPassword=%s,secretCode=%s", forgotPassDto.getEmail(), user.getId(), forgotPassDto.getNewPassword(), jwtService.generateResetPasswordToken(user.getUsername()));
        kafkaTemplate.send("comfirm-forgot-password-topic", message);

    }

    @Override
    public void comfirmReset(Long userId, String secretCode, String newPassword) {
        log.info("In AuthenticationServiceImpl.comfirmReset={}",userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        String username;
        try {
            //extrac nếu token hết hạn catch bắt ngay
            username = jwtService.extractUsername(TokenType.FORGOT_PASSWORD_TOKEN, secretCode);
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("token expired");
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid token");
        }
        if (!user.getUsername().equals(username)) {
            throw new TokenExpiredException("token expired");
        }
        if (!jwtService.isTokenValid(TokenType.FORGOT_PASSWORD_TOKEN, secretCode)) {
            throw new InvalidTokenException("Invalid token");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        BlackListTokenRefresh search = blackListTokenRefreshRepository.findByUserId(user.getId());
        if (search == null) {
            BlackListTokenRefresh blackListTokenRefresh = new BlackListTokenRefresh();
            blackListTokenRefresh.setUser(user);
            blackListTokenRefresh.setTokenBlack(user.getTokenRefresh());
            blackListTokenRefreshRepository.save(blackListTokenRefresh);
            user.setBlackListTokenRefresh(blackListTokenRefresh);
        } else {
            search.setTokenBlack(user.getTokenRefresh());
            blackListTokenRefreshRepository.save(search);
        }
        userRepository.save(user);
    }

    @Override
    public void logOut(String username) {
        log.info("In AuthenticationServiceImpl.logOut={}", username);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        user.setTokenRefreshStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }


    private void createSystem() {
        log.info("create system");
        String[] role = {UserType.SYSTEM.name(), UserType.ADMIN.name(), UserType.USER.name(), UserType.ADMIN.name()};
        Arrays.stream(role).forEach(roles -> {
            Role roleSave = roleRepository.save(
                    Role.builder()
                            .name(roles)
                            .build());
        });
        String[] permission = {"FULL", "POST", "UPDATE", "NONE"};
        Arrays.stream(permission).forEach(permissions -> {
            Permission permissionSave = permissionRepository.save(
                    Permission.builder()
                            .name(permissions)
                            .build()
            );
        });
        User system = userRepository.save(User.builder()
                .username(usernameSystem)
                .password(passwordEncoder.encode(passwordSystem))
                .email(emailSystem)
                .status(UserStatus.ACTIVE)
                .build());
        Role roleSystem = roleRepository.findById(1).orElseThrow(() -> new RuntimeException("role not found"));
        userHasRoleRepository.save(UserHasRole.builder()
                .user(system)
                .role(roleSystem)
                .build()
        );
        roleHasPermissionRepository.save(RoleHasPermission.builder()
                .role(roleSystem)
                .permission(permissionRepository.findById(1).orElseThrow(() -> new RuntimeException("permission not found")))
                .build()
        );
        roleHasPermissionRepository.save(RoleHasPermission.builder()
                .role(roleRepository.findById(2).orElseThrow(() -> new RuntimeException("role not found")))
                .permission(permissionRepository.findById(2).orElseThrow(() -> new RuntimeException("permission not found")))
                .build());
        roleHasPermissionRepository.save(RoleHasPermission.builder()
                .role(roleRepository.findById(4).orElseThrow(() -> new RuntimeException("role not found")))
                .permission(permissionRepository.findById(3).orElseThrow(() -> new RuntimeException("permission not found")))
                .build());
        roleHasPermissionRepository.save(RoleHasPermission.builder()
                .role(roleRepository.findById(3).orElseThrow(() -> new RuntimeException("role not found")))
                .permission(permissionRepository.findById(4).orElseThrow(() -> new RuntimeException("permission not found")))
                .build());
    }
}



