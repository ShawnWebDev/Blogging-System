package com.webdev.bloggingsystem;

import com.webdev.bloggingsystem.blog.BlogEntryDao;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxTrigger;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.FragmentsRendering;


@Controller
public class HomeController {

    private final BlogEntryDao blogEntryDao;

    public HomeController(BlogEntryDao blogEntryDao) {
        this.blogEntryDao = blogEntryDao;
    }

    @GetMapping("/")
    public FragmentsRendering home(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest) {
        if (!htmxRequest.isHtmxRequest()) {
            model.addAttribute("fromAbout", true);
            return FragmentsRendering.fragment("index").build();
        }
        if (htmxRequest.getCurrentUrl() == null || !htmxRequest.getCurrentUrl().endsWith("/")) {
            htmxResponse.setPushUrl("/");
        }
        return FragmentsRendering
                .fragment("index::welcome-title")
                .fragment("index::about-main")
                .build();
    }

    @GetMapping("/portfolio")
    public FragmentsRendering portfolio(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest) {
        if (!htmxRequest.isHtmxRequest()) {
            model.addAttribute("fromPortfolio", true);
            return FragmentsRendering.fragment("portfolio").build();
        }
        if (htmxRequest.getCurrentUrl() == null || !htmxRequest.getCurrentUrl().endsWith("/portfolio")) {
            htmxResponse.setPushUrl("/portfolio");
        }

        return FragmentsRendering
                .fragment("portfolio::portfolio-title")
                .fragment("portfolio::portfolio-main")
                .build();
    }

    @GetMapping("/contact")
    public FragmentsRendering contact(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest) {
        if (!htmxRequest.isHtmxRequest()) {
            model.addAttribute("fromContact", true);
            return FragmentsRendering.fragment("contact").build();
        }
        if (htmxRequest.getCurrentUrl() == null || !htmxRequest.getCurrentUrl().endsWith("/contact")) {
            htmxResponse.setPushUrl("/contact");
        }

        return FragmentsRendering.fragment("contact::contact-main").build();
    }

    @HxTrigger("loginSuccess")
    @GetMapping("/loginSuccess")
    public FragmentsRendering loginSuccess() {
        return FragmentsRendering
                .fragment("components/auth-components::logout-button")
                .header("HX-Trigger", "loginSuccess")
                .build();
    }

    @HxTrigger("logoutSuccess")
    @GetMapping("/logoutSuccess")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok()
                .header("HX-Redirect", "/blog?logout=true")
                .build();
    }

    @GetMapping("/loginError")
    public FragmentsRendering loginError(Model model) {
        model.addAttribute("loginError", true);
        return FragmentsRendering
                .fragment("components/auth-components::login-dialog")
                .build();
    }

}
