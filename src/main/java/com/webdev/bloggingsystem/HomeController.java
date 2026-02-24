package com.webdev.bloggingsystem;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxTrigger;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.FragmentsRendering;


@Controller
public class HomeController {

    @GetMapping("/")
    public FragmentsRendering home(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest) {
        if (!htmxRequest.isHtmxRequest()) {
            return FragmentsRendering.fragment("index").build();
        }
        if (htmxRequest.getCurrentUrl() == null || !htmxRequest.getCurrentUrl().endsWith("/")) {
            htmxResponse.setPushUrl("/");
        }
        return FragmentsRendering.fragment("index::about-main").build();
    }

    @HxTrigger("loginSuccess")
    @GetMapping("/loginSuccess")
    public FragmentsRendering loginSuccess() {
        return FragmentsRendering.fragment("components/auth-components::logout-form").build();
    }

    @HxTrigger("logoutSuccess")
    @GetMapping("/logoutSuccess")
    public FragmentsRendering logout(Model model) {

        model.addAttribute("logout", true);
        return FragmentsRendering.fragment("components/auth-components::login-form").build();
    }

    @GetMapping("/loginError")
    public FragmentsRendering loginError(Model model) {
        model.addAttribute("loginError", true);
        return FragmentsRendering.fragment("components/auth-components::login-form").build();
    }

}
