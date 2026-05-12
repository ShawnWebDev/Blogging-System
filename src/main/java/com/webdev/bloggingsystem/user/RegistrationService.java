package com.webdev.bloggingsystem.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;


@Service
public class RegistrationService {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);

    private final AppUserDao appUserDao;
    private final PasswordEncoder passwordEncoder;
    private final BlogSystemUserDetailsService userDetailsService;

    public RegistrationService(AppUserDao appUserDao, PasswordEncoder passwordEncoder, BlogSystemUserDetailsService userDetailsService) {
        this.appUserDao = appUserDao;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
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
        Instant otpExpires = this.getExpirationFromNow();
        logger.info("Expires: {}", otpExpires);

        appUserDao.insertVerification(otpRand, userId, otpExpires);
    }

    public int getRandomOtp() {
        SecureRandom random = new SecureRandom();
        return random.nextInt(100000,1000000);
    }

    public Instant getExpirationFromNow() {
        // expires in 15min (900 seconds) from now.
        return Instant.now().plusSeconds(900);
    }

    public Instant resetOtp(String username) {
        Integer userId = appUserDao.findUserIdByUsername(username);
        int otpRand = this.getRandomOtp();
        Instant otpExpires = this.getExpirationFromNow();
        appUserDao.updateVerification(otpRand, userId, otpExpires);
        // this.sendOtp();
        return otpExpires;
    }

    // todo: send otp to email..
    public void sendOtp(String username) {
        logger.info("Sending OTP for user {}", username);
        //
    }

    // todo: verify entered otp and user from last login in session..
    public String verifyUser(String token, String username) {

        return "";
    }

}