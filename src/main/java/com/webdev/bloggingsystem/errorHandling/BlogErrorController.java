package com.webdev.bloggingsystem.errorHandling;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;
import java.net.URISyntaxException;


@Controller
public class BlogErrorController implements ErrorController {
    private static final Logger logger = LoggerFactory.getLogger(BlogErrorController.class);

    @RequestMapping("/error")
    public Object handleError(Model model, HttpServletRequest request) {
        Integer status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        // handle 403 status for invalid Session or CSRF token,
        // if a state changing request happens after expiration, re-direct to /URI?sessionExpired informing user to log in again.
        if (status != null && status == HttpStatus.FORBIDDEN.value()) {
            String refererUrl = request.getHeader("Referer");
            String redirectParam = "?sessionExpired=true";
            String redirectUrl = "/blog";
            // ensure user stays on page if single post page
            if (refererUrl != null) {
                try {
                    refererUrl = new URI(refererUrl).getPath();
                    if (refererUrl != null && refererUrl.startsWith("/blog/post")) {
                        redirectUrl = refererUrl;
                    }
                } catch (URISyntaxException e) {
                    logger.warn("Incorrect referer header: {}. ** 'redirect' is default Redirect.", refererUrl);
                }
            }

            boolean isHtmx = "true".equals(request.getHeader("HX-Request"));
            if (isHtmx) {
                String hxLocation = String.format(
                        "{\"path\":\"%s\", \"target\":\"#main-content\", \"swap\":\"outerHTML\"}", redirectUrl + redirectParam
                );

                return ResponseEntity.ok()
                        .header("HX-Location", hxLocation)
                        .build();
            }

            return "redirect:" + redirectUrl + redirectParam;
        }

        // handle other errors
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
