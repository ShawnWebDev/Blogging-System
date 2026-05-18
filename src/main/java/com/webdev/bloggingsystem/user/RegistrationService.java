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
import java.util.Arrays;


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
        logger.info("Expires: {}", otpExpires);

        appUserDao.insertVerification(otpRand, userId, otpExpires);

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
        int otpRand = this.getRandomOtp();
        Instant otpExpires = this.getExpirationFromNow();

        if (userIdEmail == null) {
            throw new BlogEntryException("Username not found!");
        }

        int userId = (int) userIdEmail[0];
        String email = (String) userIdEmail[1];

        appUserDao.deleteOtpDetailsByUserId(userId);
        appUserDao.insertVerification(otpRand, userId, otpExpires);

        try {
            emailService.sendOtp(email, otpRand);
        } catch (MessagingException e) {
            logger.error("Error sending OTP", e);
            throw new BlogEntryException("Error sending OTP.");
        }

        return otpExpires;
    }

    public VerificationStatus verifyUser(String username, int otp) {
        // [int userId, int otp, Instant expiry]
        // get by username (based on session and correct credentials) ensures userId matches username
        Object[] otpDetails = appUserDao.getOtpDetailsByUsername(username);
        logger.info("Verifying user {}", Arrays.toString(otpDetails));
        if (otpDetails == null || otpDetails.length != 3) {
            return VerificationStatus.EXPIRED;
        }

        int userId = (int) otpDetails[0];
        int savedOtp = (int) otpDetails[1];
        Instant otpValidUntil = (Instant) otpDetails[2];

        if (otp != savedOtp) {
            return VerificationStatus.INVALID;
        }
        if (otpValidUntil.isBefore(Instant.now())) {
            return VerificationStatus.EXPIRED;
        }

        appUserDao.updateUserActive(username);
        appUserDao.deleteOtpDetailsByUserId(userId);

        return VerificationStatus.VALID;
    }

}