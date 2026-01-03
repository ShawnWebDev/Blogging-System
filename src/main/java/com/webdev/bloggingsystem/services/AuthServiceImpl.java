package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.dto.LoginDto;
import com.webdev.bloggingsystem.dto.RegisterDto;
import com.webdev.bloggingsystem.dto.UserProfile;

import com.webdev.bloggingsystem.entities.AppUser;
import com.webdev.bloggingsystem.entities.Role;
import com.webdev.bloggingsystem.entities.RoleType;
import com.webdev.bloggingsystem.repositories.AppUserRepo;
import com.webdev.bloggingsystem.repositories.RoleRepo;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {
    private final JwtEncoder jwtEncoder;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final AppUserRepo appUserRepo;
    private final RoleRepo roleRepo;


    public AuthServiceImpl(JwtEncoder jwtEncoder, AuthenticationManager authenticationManager, AppUserRepo appUserRepo,
                           RoleRepo roleRepo, PasswordEncoder passwordEncoder) {
        this.jwtEncoder = jwtEncoder;
        this.authenticationManager = authenticationManager;
        this.appUserRepo = appUserRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
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
    public void register(RegisterDto registerDto) {
        String password = passwordEncoder.encode(registerDto.password());
        AppUser appUser = new AppUser(registerDto.username(), password, registerDto.email());
        Role roles = roleRepo.findByRole(RoleType.USER);
        appUser.setRoles(Set.of(roles));

        appUserRepo.save(appUser);
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