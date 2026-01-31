package com.webdev.bloggingsystem.controllers;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/blog")
    public String blog() {
        return "blog";
    }


    @GetMapping("/loginSuccess")
    public String loginSuccess(HttpServletResponse response) {
        response.setHeader("HX-Trigger", "loginSuccess");
        return "components/header-components::auth-head";
    }

    @GetMapping("/loginError")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "/components/header-components::login-form";
    }

    @GetMapping("/logoutSuccess")
    public String logout(Model model, HttpServletResponse response) {
        response.setHeader("HX-Trigger", "logoutSuccess");
        model.addAttribute("logout", true);
        return "/components/header-components::login-form";
    }

    @GetMapping("/commentForm")
    public String commentForm() {
        return "/components/comment-components::comment-form-enabled";
    }

    @GetMapping("/removeCommentForm")
    public String remove() {
        return "/components/comment-components::comment-form-disabled";
    }

}
