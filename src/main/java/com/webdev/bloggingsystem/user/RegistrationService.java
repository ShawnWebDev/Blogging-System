package com.webdev.bloggingsystem.user;

import com.webdev.bloggingsystem.errorHandling.BlogEntryException;

import jakarta.mail.MessagingException;
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
    private final EmailService emailService;

    private final SecureRandom random = new SecureRandom();

    public RegistrationService(AppUserDao appUserDao, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.appUserDao = appUserDao;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public Instant otpValidUntil(String username) {
        // username is verified to exist at this point.
        return appUserDao.getOtpExpirationByUsername(username);
    }

    @Transactional
    public void registerUser(UserRegistrationDto dto) {
        AppUser appUser = new AppUser(
                dto.getUsername(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getEmail()
        );
        appUser.setRole(RoleType.USER);
        dto.setPassword(null);

        logger.info("Registering user {}", appUser.getUsername());
        int userId = appUserDao.insert(appUser);
        int otpRand = this.getRandomOtp();
        Instant otpExpires = this.getExpirationFromNow();

        appUserDao.insertVerification(passwordEncoder.encode(String.valueOf(otpRand)), userId, otpExpires);

        try {
            emailService.sendOtp(appUser.getEmail(), otpRand);
        } catch (MessagingException e) {
            logger.error("Error sending OTP", e);
            throw new BlogEntryException("Error sending OTP.");
        }
    }

    public int getRandomOtp() {
        return random.nextInt(100000,1000000);
    }

    public Instant getExpirationFromNow() {
        // expires in 15min (900 seconds) from now.
        return Instant.now().plusSeconds(900);
    }

    @Transactional
    public Instant resetOtp(String username) {
        // [userId, email]
        Object[] userIdEmail = appUserDao.getUserIdEmailByUsername(username);
        if (userIdEmail == null) {
            throw new BlogEntryException("Username not found!");
        }

        int otpRand = this.getRandomOtp();
        Instant otpExpires = this.getExpirationFromNow();

        int userId = (int) userIdEmail[0];
        String email = (String) userIdEmail[1];

        appUserDao.deleteOtpDetailsByUserId(userId);
        appUserDao.insertVerification(passwordEncoder.encode(String.valueOf(otpRand)), userId, otpExpires);

        try {
            emailService.sendOtp(email, otpRand);
        } catch (MessagingException e) {
            logger.error("Error sending OTP", e);
            throw new BlogEntryException("Error sending OTP.");
        }

        return otpExpires;
    }

    public VerificationStatus verifyUser(String username, String otp) {
        // [int userId, int otp, Instant expiry]
        // get by username (based on session and correct credentials) ensures userId matches username
        Object[] otpDetails = appUserDao.getOtpDetailsByUsername(username);
        if (otpDetails == null || otpDetails.length != 3) {
            logger.warn("OTP details incorrect for user: {}. Otp length: {}.",
                    username, otpDetails == null ? "null" : otpDetails.length);
            return VerificationStatus.EXPIRED;
        }

        int userId = (int) otpDetails[0];
        String savedOtp = (String) otpDetails[1];
        Instant otpValidUntil = (Instant) otpDetails[2];

        if (otpValidUntil.isBefore(Instant.now())) {
            return VerificationStatus.EXPIRED;
        }
        if (!passwordEncoder.matches(otp, savedOtp)) {
            return VerificationStatus.INVALID;
        }

        appUserDao.updateUserActive(username);
        appUserDao.deleteOtpDetailsByUserId(userId);

        return VerificationStatus.VALID;
    }

}