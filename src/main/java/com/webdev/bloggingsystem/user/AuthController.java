package com.webdev.bloggingsystem.user;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.FragmentsRendering;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;

@Controller
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final RegistrationService registrationService;

    public AuthController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /**
     * <p> Spring Security redirects here on a successful login.
     * Uses HttpServletRequest and not HtmxRequest because the request is from a redirect by Spring Security.
     * <p> Login referer can be from /blog or any /single-post page (for commenting).
     * The getPath() used on isFromBlog removes URI params to correctly check the String '/blog' by removing /blog?logout or /blog?sessionExpired
     * <p> The logout-button template fragment contains admin controls. These are filtered out by Thymeleaf if Auth does not have the ADMIN role.
    */
    @GetMapping("/loginSuccess")
    public FragmentsRendering loginSuccess(HttpServletRequest request, HtmxResponse htmxResponse) {
        String referer = request.getHeader("Referer");
        if (this.isFromBlog(referer)) {
            htmxResponse.setPushUrl("/blog");
            return FragmentsRendering
                    .fragment("components/auth-components::logout-button-blog")
                    .fragment("components/auth-components::csrf-token-oob")
                    .header("HX-Trigger", "loginSuccess")
                    .build();
        }
        return FragmentsRendering
                .fragment("components/auth-components::logout-button-post")
                .fragment("components/auth-components::csrf-token-oob") // to refresh the csrf token with an out-of-band swap
                .header("HX-Trigger", "{\"loginSuccess\": \""+ SecurityContextHolder.getContext().getAuthentication().getName() + "\"}")
                .build();
    }

    @GetMapping("/logoutSuccess")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok()
                .header("HX-Redirect", "/blog?logout")
                .build();
    }
/**
 * <p> On login error, this method retargets login form submission to its container to re-render the error messages and buttons if needed.
 * <p> Uses the current session to get the PENDING_VERIFICATION_USER attribute set by the LoginFailureHandler after a DisabledException.
 * The attribute is set only after registration OR if login credentials were valid but the user has isActive set to false in the DB. (Enabled = false in UserDetails Object)
  */
    @GetMapping("/loginError")
    public FragmentsRendering loginError(Model model, HttpServletResponse response, HttpServletRequest request) {
        HttpSession httpSession = request.getSession(false);
        String errorMsg = "Invalid username or password.";

        if (httpSession != null) {
            String username = (String) httpSession.getAttribute("PENDING_VERIFICATION_USER");
            if (username != null) {
                errorMsg = "Account not verified. ";
                Instant otpValidUntil = registrationService.otpValidUntil(username);

                if (otpValidUntil.isAfter(Instant.now())) {
                    errorMsg += "Your one time password is valid until ";
                    model.addAttribute("validUntil", otpValidUntil);
                } else {
                    errorMsg += "Your one time password has expired.";
                    model.addAttribute("expired", true);
                }
            }
        }
        model.addAttribute("loginError", errorMsg);
        response.addHeader("HX-Retarget", "#login-article");

        return this.loginView();
    }

    @HxRequest
    @GetMapping("/loginForm")
    public FragmentsRendering loginView() {
        return FragmentsRendering
                .fragment("components/auth-components::login-article")
                .fragment("components/auth-components::csrf-token-oob")
                .build();
    }

    @HxRequest
    @GetMapping("/register")
    public String registrationView(Model model) {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto();

        model.addAttribute("userDto", userRegistrationDto);

        return "components/auth-components::register-article";
    }

    //todo: if DTO is valid, registration service generates OTP, sends email,
    // user enters OTP, set their enabled flag using email to get username/role, log them in, delete OTP row in verification table
    @HxRequest
    @PostMapping("/register")
    public String registration(Model model, HttpServletRequest request,
                               @Valid @ModelAttribute("userDto") UserRegistrationDto userRegistrationDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            userRegistrationDto.setPassword(null);
            userRegistrationDto.setConfirmPassword(null);
            model.addAttribute("userDto", userRegistrationDto);
            return "components/auth-components::register-article";
        }

        registrationService.registerUser(userRegistrationDto);

        String username = userRegistrationDto.username;
        model.addAttribute("validUntil", registrationService.otpValidUntil(username));
        request.getSession(true).setAttribute("PENDING_VERIFICATION_USER", username);

        return  "components/auth-components::validate-prompt-article";
    }

    // todo - this opens the form to enter the otp,
    @HxRequest
    @GetMapping("/validate")
    public Object validateView(Model model, HttpServletRequest request) {
        HttpSession httpSession = request.getSession(false);
        if (httpSession != null) {
            String username = (String) httpSession.getAttribute("PENDING_VERIFICATION_USER");
            if (username != null) {
                model.addAttribute("validUntil", registrationService.otpValidUntil(username));
                return  "components/auth-components::validate-prompt-article";
            }
        }
        return this.loginView();
    }

    @HxRequest
    @PostMapping("/validate")
    public Object verifyAccount(Model model, HttpServletRequest request, @RequestParam Integer otp) {
        HttpSession httpSession = request.getSession(false);
        if (httpSession != null) {
            String username = (String) httpSession.getAttribute("PENDING_VERIFICATION_USER");
            if (username != null) {
                logger.info("user: {} --- otp entered: {}", username, otp);
                String referer = request.getHeader("Referer");
                String validationStatus = registrationService.verifyUser(request, username, otp);
                switch (validationStatus) {
                    case "valid":
                        try {
                            String path = new URI(referer).getPath();
                            return ResponseEntity.ok()
                                    .header("HX-Redirect", path)
                                    .build();
                        } catch (URISyntaxException e) {
                            logger.warn("{} ** Incorrect referer header: {}. ** 'isFromBlog' is defaulted to false.", e, referer);
                        }
                        break;
                    case "invalid":
                        model.addAttribute("validUntil", registrationService.otpValidUntil(username));
                        model.addAttribute("otpError", "Invalid code!");
                        return  "components/auth-components::validate-prompt-article";
                    case "expired":
                        model.addAttribute("expired", true);
                        return  "components/auth-components::validate-prompt-article";
                }
            }
        }

        model.addAttribute("loginError", "Unexpected error.");
        return this.loginView();
    }

    @HxRequest
    @GetMapping("/resendValidation")
    public Object resendValidation(Model model, HttpServletRequest request) {
        HttpSession httpSession = request.getSession(false);
        if (httpSession != null) {
            String username = (String) httpSession.getAttribute("PENDING_VERIFICATION_USER");
            if (username != null) {
                model.addAttribute("validUntil", registrationService.resetOtp(username));
                registrationService.sendOtp(username, null);
            } else {
                model.addAttribute("loginError", "Invalid username or password.");
                return this.loginView();
            }
            return  "components/auth-components::validate-prompt-article";
        }
        return this.loginView();
    }

    @HxRequest
    @GetMapping("/refresh-token")
    public String refreshToken() {
        return "components/auth-components::csrf-token-oob";
    }


    private boolean isFromBlog(String referer) {
        boolean isFromBlog = false;
        try {
            isFromBlog = referer != null && new URI(referer).getPath().equals("/blog");
        } catch (URISyntaxException e) {
            logger.warn("{} ** Incorrect referer header: {}. ** 'isFromBlog' is defaulted to false.", e, referer);
        }
        return isFromBlog;
    }
}
