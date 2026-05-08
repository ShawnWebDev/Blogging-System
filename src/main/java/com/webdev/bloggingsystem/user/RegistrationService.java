package com.webdev.bloggingsystem.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;


@Service
public class RegistrationService {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);

    private final AppUserDao appUserDao;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(AppUserDao appUserDao, PasswordEncoder passwordEncoder) {
        this.appUserDao = appUserDao;
        this.passwordEncoder = passwordEncoder;
    }

    // TODO

    public Instant otpValidUntil(String username) {
        // username is verified to exist at this point.
        Integer userId = appUserDao.findUserIdByUsername(username);
        Instant expiry = appUserDao.getExpires(userId);
        if (expiry == null) {
            return Instant.now();
        }
        return expiry;
    }

    @Transactional
    public void registerUser(UserRegistrationDto dto) {
        AppUser appUser = new AppUser(
                dto.username,
                passwordEncoder.encode(dto.password),
                dto.email
        );
        appUser.setRole(RoleType.USER);
        dto.setPassword(null);
        logger.info("Registering user {}", appUser.getUsername());
        int userId = appUserDao.insert(appUser);

        int otpRand = this.getRandomOtp();
        // expires in 15min (900 seconds) from now.
        Instant otpExpires = Instant.now().plusSeconds(900);
        logger.info("Expires: {}", otpExpires);

        appUserDao.insertVerification(otpRand, userId, otpExpires);
    }

    public int getRandomOtp() {
        SecureRandom random = new SecureRandom();
        return random.nextInt(100000,1000000);
    }

    /*
    public String verifyUser(String token) {

        return "";
    }
    */

}