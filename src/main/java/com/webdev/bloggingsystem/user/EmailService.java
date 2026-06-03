package com.webdev.bloggingsystem.user;

import jakarta.mail.MessagingException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String email, int otp) throws MessagingException {
        mailSender.send(mimeMessage -> {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            message.setFrom("shawns.portfolio.blog@gmail.com");
            message.setTo(email);
            message.setSubject("Your One Time Password.");
            message.setText(
                    "<p>Enter this 6 digit code to validate your account:</p>" +
                    "<p>" + otp + "</p>" +
                    "<p>If you closed or refreshed the tab or window, you must log in using the credentials you registered with and follow the prompts.</p>",
                    true);
        });
    }
}
