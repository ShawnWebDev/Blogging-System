package com.webdev.bloggingsystem.errorHandling;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.view.FragmentsRendering;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BlogEntryException.class)
    public FragmentsRendering handleError(BlogEntryException ex, Model model, HtmxRequest request) {
        model.addAttribute("errorMsg", ex.getMessage());
        model.addAttribute("title", "Error!");

        if (!request.isHtmxRequest()) {
            return FragmentsRendering
                    .fragment("error/error-components")
                    .build();
        }

        return FragmentsRendering
                .fragment("error/error-components::error-field")
                .header("HX-Retarget", "#error-field")
                .build();
    }

}
