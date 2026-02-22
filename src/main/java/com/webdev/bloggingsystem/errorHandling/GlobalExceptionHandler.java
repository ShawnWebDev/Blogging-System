package com.webdev.bloggingsystem.errorHandling;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxReswap;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.view.FragmentsRendering;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BlogEntryException.class)
    public FragmentsRendering handleError(BlogEntryException ex, Model model, HtmxResponse response) {
        response.setReswap(HtmxReswap.none());
        model.addAttribute("errorMsg", ex.getMessage());
        return FragmentsRendering
                .fragment("components/error-components::error-message")
                .build();
    }

}
