package com.webdev.bloggingsystem;

import com.webdev.bloggingsystem.blog.BlogService;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxTrigger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.FragmentsRendering;

import java.net.URI;
import java.net.URISyntaxException;


@Controller
public class HomeController {

    private final BlogService blogEntryService;
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    public HomeController(BlogService blogEntryService) {
        this.blogEntryService = blogEntryService;
    }

    @GetMapping("/")
    public Object home(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest) {
        model.addAttribute("title", "Shawn Osborne's Website");

        if (!htmxRequest.isHtmxRequest()) {
            model.addAttribute("fromAbout", true);
            return "index";
        }
        pushUrlIfNeeded(htmxRequest, htmxResponse, "/");

        return FragmentsRendering
                .fragment("components/shared-head::head-title")
                .fragment("index::about-main")
                .build();
    }

    // todo : get posts with 'portfolio' category and send view
    @GetMapping("/portfolio")
    public Object portfolio(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest) {
        model.addAttribute("title", "Portfolio | Shawn Osborne");

        if (!htmxRequest.isHtmxRequest()) {
            model.addAttribute("fromPortfolio", true);
            return "portfolio";
        }
        pushUrlIfNeeded(htmxRequest, htmxResponse, "/portfolio");

        return FragmentsRendering
                .fragment("components/shared-head::head-title")
                .fragment("portfolio::portfolio-main")
                .build();
    }

    @GetMapping("/contact")
    public Object contact(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest) {
        model.addAttribute("title", "Contact | Shawn Osborne");

        if (!htmxRequest.isHtmxRequest()) {
            model.addAttribute("fromContact", true);
            return "contact";
        }
        pushUrlIfNeeded(htmxRequest, htmxResponse, "/contact");

        return FragmentsRendering
                .fragment("components/shared-head::head-title")
                .fragment("contact::contact-main")
                .build();
    }

    @HxTrigger("loginSuccess")
    @GetMapping("/loginSuccess")
    public FragmentsRendering loginSuccess(HttpServletRequest request, HtmxResponse htmxResponse) {
        // called from Spring Security redirect on successful log in, does not use HtmxRequest because is from redirect by Security
        String referer = request.getHeader("Referer");
        boolean isFromBlog = false;
        try {
            // getPath() strips params to correctly send fragment if /blog, /blog?logout, or /blog?sessionExpired
            isFromBlog = referer != null && new URI(referer).getPath().equals("/blog");
        } catch (URISyntaxException e) {
            logger.warn("Incorrect referer header: {}. ** 'isFromBlog' is defaulted to false.", referer);
        }

        if (isFromBlog) {
            // clear ?logout and ?sessionExpired params after login success
            htmxResponse.setPushUrl("/blog");
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
                .header("HX-Redirect", "/blog?logout")
                .build();
    }

    @GetMapping("/loginError")
    public String loginError(Model model, HttpServletResponse response) {
        model.addAttribute("loginError", true);
        response.addHeader("HX-Retarget", "#login-article");
        return "components/auth-components::login-article";
    }

    private static void pushUrlIfNeeded(HtmxRequest request, HtmxResponse response, String url) {
        if (request.getCurrentUrl() == null || !request.getCurrentUrl().endsWith(url)) {
            response.setPushUrl(url);
        }
    }


}
