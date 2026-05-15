package com.webdev.bloggingsystem.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {
    @Mock
    AppUserDao appUserDao;

    @InjectMocks
    RegistrationService registrationService;

    @Test
    void verifyUserHappyPath() {
        // create expiry time greater than time test is ran
        Instant timeToExpiry = Instant.now().plusSeconds(900);
        when(appUserDao.getOtpDetailsByUsername(anyString())).thenReturn(new Object[]{1, 123456, timeToExpiry});
        VerificationStatus verificationStatus = registrationService.verifyUser("Test User", 123456);

        assertEquals(VerificationStatus.VALID, verificationStatus);
    }

    @Test
    void verifyUserNullOtpDetail() {
        when(appUserDao.getOtpDetailsByUsername(anyString())).thenReturn(null);
        VerificationStatus verificationStatus = registrationService.verifyUser("Test User", 123456);

        assertEquals(VerificationStatus.EXPIRED, verificationStatus);
    }

    @Test
    void verifyUserInvalidOtp() {
        Instant timeToExpiry = Instant.now().plusSeconds(900);
        when(appUserDao.getOtpDetailsByUsername(anyString())).thenReturn(new Object[]{1, 123456, timeToExpiry});
        VerificationStatus verificationStatus = registrationService.verifyUser("Test User", 100000);

        assertEquals(VerificationStatus.INVALID, verificationStatus);
    }

    @Test
    void verifyUserExpiredOtp() {
        Instant timeToExpiry = Instant.now().minusSeconds(10);
        when(appUserDao.getOtpDetailsByUsername(anyString())).thenReturn(new Object[]{1, 123456, timeToExpiry});
        VerificationStatus verificationStatus = registrationService.verifyUser("Test User", 123456);

        assertEquals(VerificationStatus.EXPIRED, verificationStatus);
    }
}
