package com.webdev.bloggingsystem;

import com.webdev.bloggingsystem.blog.BlogService;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxTrigger;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.FragmentsRendering;


@Controller
public class HomeController {

    private final BlogService blogEntryService;

    public HomeController(BlogService blogEntryService) {
        this.blogEntryService = blogEntryService;
    }

    @GetMapping("/")
    public FragmentsRendering home(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest) {
        model.addAttribute("title", "Shawn Osborne's Website");
        if (!htmxRequest.isHtmxRequest()) {
            model.addAttribute("fromAbout", true);
            return FragmentsRendering.fragment("index").build();
        }
        if (htmxRequest.getCurrentUrl() == null || !htmxRequest.getCurrentUrl().endsWith("/")) {
            htmxResponse.setPushUrl("/");
        }
        return FragmentsRendering
                .fragment("components/shared-head::head-title")
                .fragment("index::about-main")
                .build();
    }

    // todo : get posts with 'portfolio' category and render/send view, should i inject BlogEntryDao here or in constructor?
    @GetMapping("/portfolio")
    public FragmentsRendering portfolio(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest) {
        model.addAttribute("title", "Portfolio | Shawn Osborne");
        if (!htmxRequest.isHtmxRequest()) {
            model.addAttribute("fromPortfolio", true);
            return FragmentsRendering.fragment("portfolio").build();
        }
        if (htmxRequest.getCurrentUrl() == null || !htmxRequest.getCurrentUrl().endsWith("/portfolio")) {
            htmxResponse.setPushUrl("/portfolio");
        }

        return FragmentsRendering
                .fragment("components/shared-head::head-title")
                .fragment("portfolio::portfolio-main")
                .build();
    }

    @GetMapping("/contact")
    public FragmentsRendering contact(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest) {
        model.addAttribute("title", "Contact | Shawn Osborne");
        if (!htmxRequest.isHtmxRequest()) {
            model.addAttribute("fromContact", true);
            return FragmentsRendering.fragment("contact").build();
        }
        if (htmxRequest.getCurrentUrl() == null || !htmxRequest.getCurrentUrl().endsWith("/contact")) {
            htmxResponse.setPushUrl("/contact");
        }

        return FragmentsRendering
                .fragment("components/shared-head::head-title")
                .fragment("contact::contact-main")
                .build();
    }

    @HxTrigger("loginSuccess")
    @GetMapping("/loginSuccess")
    public FragmentsRendering loginSuccess(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        boolean isFromBlog = referer != null && referer.endsWith("/blog");
        if (isFromBlog) {
            return FragmentsRendering
                    .fragment("components/auth-components::logout-button-blog")
                    .fragment("components/auth-components::csrf-token-oob")
                    .build();
        }
        return FragmentsRendering
                .fragment("components/auth-components::logout-button-post")
                .fragment("components/auth-components::csrf-token-oob") // to refresh the csrf token with an out-of-band swap
                .build();
    }

    @GetMapping("/logoutSuccess")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok()
                .header("HX-Refresh", "true")
                .build();
    }

    @GetMapping("/loginError")
    public FragmentsRendering loginError(Model model) {
        model.addAttribute("loginError", true);
        return FragmentsRendering
                .fragment("components/auth-components::login-article")
                .header("HX-Retarget", "#login-article")
                .build();
    }

}
