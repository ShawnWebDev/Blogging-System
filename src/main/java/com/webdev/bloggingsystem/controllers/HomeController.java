package com.webdev.bloggingsystem.controllers;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.FragmentsRendering;


@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model, HttpServletResponse response) {
        model.addAttribute("heading", "Hello World!");
        response.setHeader("HX-Push-Url", "/");
        return "index";
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
