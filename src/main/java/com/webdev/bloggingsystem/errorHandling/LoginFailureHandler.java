package com.webdev.bloggingsystem.errorHandling;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException ex) throws IOException, ServletException {

        if (ex instanceof DisabledException) {
            // fires when logging in to account that has not been verified by email,
            // reset session, clear authentication context from thread-local, store username in new session for use in otp validation
            String username = request.getParameter("username");
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            SecurityContextHolder.clearContext();
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("PENDING_VERIFICATION_USER", username);
        } else {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute("PENDING_VERIFICATION_USER");
            }
        }

        super.setDefaultFailureUrl("/loginError");
        super.onAuthenticationFailure(request, response, ex);
    }
}
