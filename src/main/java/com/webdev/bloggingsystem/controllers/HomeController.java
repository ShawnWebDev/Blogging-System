package com.webdev.bloggingsystem.controllers;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.FragmentsRendering;


@Controller
public class HomeController {

    @GetMapping("/")
    public View home(Model model, HttpServletResponse response,
                       @RequestHeader(value = "HX-Request", required = false) boolean isHtmx,
                       @RequestHeader(value = "HX-Current-URL", required = false) String currentUrl) {
        model.addAttribute("heading", "Hello World!");

        if (currentUrl == null || !currentUrl.endsWith("/")) {
            response.setHeader("HX-Push-Url", "/");
        }

        if (isHtmx) {
            return FragmentsRendering.fragment("components/header-components::simple-header").fragment("index::about-main").build();
        }
        return FragmentsRendering.fragment("index").build();
    }

    @GetMapping("/loginSuccess")
    public View loginSuccess(HttpServletResponse response) {
        response.setHeader("HX-Trigger", "loginSuccess");
        return FragmentsRendering.fragment("components/auth-components::logout-form").build();
    }

    @GetMapping("/logoutSuccess")
    public View logout(Model model, HttpServletResponse response) {
        response.setHeader("HX-Trigger", "logoutSuccess");
        model.addAttribute("logout", true);
        return FragmentsRendering.fragment("components/auth-components::login-form").build();
    }

    @GetMapping("/loginError")
    public View loginError(Model model) {
        model.addAttribute("loginError", true);
        return FragmentsRendering.fragment("components/auth-components::login-form").build();
    }

}
