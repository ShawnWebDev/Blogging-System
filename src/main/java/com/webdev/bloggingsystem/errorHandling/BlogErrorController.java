package com.webdev.bloggingsystem.errorHandling;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class BlogErrorController implements ErrorController {
    @RequestMapping("/error")
    public String handleError(Model model, HttpServletRequest request) {
        Integer status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        model.addAttribute("title", "Error!");

        if (status == HttpStatus.NOT_FOUND.value()) {
            model.addAttribute("errorMsg", "Page Not Found! ");
        } else if (status == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            model.addAttribute("errorMsg", "Server Error, Please Try Again Later! ");
        }

        return "error/error-components";
    }
}
