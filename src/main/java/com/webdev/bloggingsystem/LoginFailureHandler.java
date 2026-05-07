package com.webdev.bloggingsystem;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException ex) throws IOException, ServletException {

        if (ex instanceof DisabledException) {
            String username = request.getParameter("username");
            request.getSession().setAttribute("PENDING_VERIFICATION_USER", username);
        } else {
            request.getSession().removeAttribute("PENDING_VERIFICATION_USER");
        }

        super.setDefaultFailureUrl("/loginError");
        super.onAuthenticationFailure(request, response, ex);
    }
}
