package com.webdev.bloggingsystem.errorHandling;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class BlogErrorController implements ErrorController {

    @RequestMapping("/error")
    public Object handleError(Model model, HttpServletRequest request) {
        Integer status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        boolean isHtmx = "true".equals(request.getHeader("HX-Request"));
        // handle 403 status for invalid Session or mismatched CSRF,
        // not likely to happen since I have the CSRF heartbeat, just a fallback..
        if (status != null && status == HttpStatus.FORBIDDEN.value()) {
            CsrfToken storedToken = (CsrfToken) request.getAttribute("_csrf");
            String requestToken = request.getHeader("X-CSRF-TOKEN");
            if (storedToken == null || !storedToken.getToken().equals(requestToken)) {
                if (isHtmx) {
                    return ResponseEntity.ok()
                            .header("HX-Location", "{\"path\":\"/blog?sessionExpired=true\", \"target\":\"#main-content\", \"swap\":\"outerHTML\"}")
                            .build();
                }
                return "redirect:/blog?sessionExpired=true";
            }
        }

        model.addAttribute("title", "Error!");
        model.addAttribute("errorMsg", this.getErrorMessage(status));
        return "error/error-components";
    }

    private String getErrorMessage(Integer sc) {
        if (sc == HttpStatus.NOT_FOUND.value()) return "Page Not Found!";
        if (sc == HttpStatus.INTERNAL_SERVER_ERROR.value()) return "Server Error!";
        return "An unexpected error occurred.";
    }
}
