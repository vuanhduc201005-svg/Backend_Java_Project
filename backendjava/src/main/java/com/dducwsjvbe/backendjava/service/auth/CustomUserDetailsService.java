package com.dducwsjvbe.backendjava.service.auth;

import com.dducwsjvbe.backendjava.model.RoleHasPermission;
import com.dducwsjvbe.backendjava.model.User;
import com.dducwsjvbe.backendjava.model.UserHasRole;
import com.dducwsjvbe.backendjava.repository.interfaces.UserRepository;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public record CustomUserDetailsService(UserRepository userRepository) implements UserDetailsService {


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

            User user = userRepository.findByUsername(username);
            if (user==null) {
                System.out.println("User not available");
                throw new UsernameNotFoundException("User not available");
            }
            List<GrantedAuthority> authorities = user.getRoles().stream().flatMap(UserHasRole -> {
                        String roleName = UserHasRole.getRole().getName();
                        return UserHasRole.getRole().getPermissions().stream().map(RoleHasPermission ->
                                roleName + "_" + RoleHasPermission.getPermission().getName()
                        );
                    }
            ).distinct().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
            return new CustomUserDetails(user, authorities);
    }
}



