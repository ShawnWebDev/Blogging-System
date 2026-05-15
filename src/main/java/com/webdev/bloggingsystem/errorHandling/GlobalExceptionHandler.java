package com.webdev.bloggingsystem.errorHandling;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BlogEntryException.class)
    public String handleError(BlogEntryException ex, Model model, HtmxRequest request, HtmxResponse response) {
        String message = ex.getMessage();
        if (message.equals("Username not found!")) {
            model.addAttribute("loginError", message);
            response.setRetarget("#login-article");
            return "components/auth-components::login-article";
        }

        model.addAttribute("errorMsg", message);
        model.addAttribute("title", "Error!");

        if (!request.isHtmxRequest()) {
            return "error/error-components";
        }

        response.setRetarget("#error-field");
        return "error/error-components::error-field";
    }

}
