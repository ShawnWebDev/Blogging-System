package com.webdev.bloggingsystem.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

    public Instant otpValidUntil(String username) {
        // username is verified to exist at this point.
        Integer userId = appUserDao.findUserIdByUsername(username);
        return appUserDao.getExpires(userId);
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

        this.sendOtp(appUser.getUsername(), appUser.getEmail());
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

    // todo: figure out email service!!!
    // todo: send otp to email..
    public void sendOtp(String username, String email) {
        logger.info("Sending OTP for user {}", username);
        //
    }

    public String verifyUser(HttpServletRequest request, String username, int otp) {
        // [int otp, Instant expiry]
        Object[] otpDetails = appUserDao.getOtpDetailsByUsername(username);

        if (otpDetails == null) {
            return "expired";
        }

        int savedOtp = (int) otpDetails[0];
        Instant otpValidUntil = (Instant) otpDetails[1];

        if (otp != savedOtp) {
            return "invalid";
        }
        if (otpValidUntil.isBefore(Instant.now())) {
            return "expired";
        }

        appUserDao.updateUserActive(username);
        // create new session, store in fresh SecurityContext
        HttpSession oldSession = request.getSession(false);
        oldSession.invalidate();
        HttpSession newSession = request.getSession(true);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        newSession.setAttribute("SPRING_SECURITY_CONTEXT", context);

        return "valid";
    }

}