package com.dducwsjvbe.backendjava.filter;

import com.dducwsjvbe.backendjava.enums.TokenType;
import com.dducwsjvbe.backendjava.service.auth.CustomUserDetailsService;
import com.dducwsjvbe.backendjava.service.interfaces.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/auth/");
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            final String token = authorizationHeader.substring(7);
            final String username = jwtService.extractUsername(TokenType.ACCESS_TOKEN, token);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null && username != null) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                if (userDetails == null) {
                    throw new UsernameNotFoundException("token failed");
                }
                if (jwtService.isTokenValid(TokenType.ACCESS_TOKEN, token)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token expired");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
/*
http://localhost:8080/actuator/metrics/http.server.requests
http://localhost:8080/actuator/metrics/http.server.requests?tag=uri:/auth/login&tag=method:POST

 */