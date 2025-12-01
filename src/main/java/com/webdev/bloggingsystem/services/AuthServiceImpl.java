package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.dto.LoginDto;
import com.webdev.bloggingsystem.dto.UserProfile;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {
    private final JwtEncoder jwtEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(JwtEncoder jwtEncoder, AuthenticationManager authenticationManager) {
        this.jwtEncoder = jwtEncoder;
        this.authenticationManager = authenticationManager;
    }

    private String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .subject(authentication.getName())
                .claim("scope", scope)
                .build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    @Override
    public String login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.username(),
                        loginDto.password()
                )
        );
        return this.generateToken(authentication);
    }

    @Override
    public void register(LoginDto loginDto, String email) {
        // todo
    }

    @Override
    public UserProfile getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String username = jwt.getSubject();

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("SCOPE_ADMIN"));

            return new UserProfile(username, isAdmin);
        }

        return null;
    }

}