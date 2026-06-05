package com.webdev.bloggingsystem;

import com.webdev.bloggingsystem.blog.BlogService;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.FragmentsRendering;


@Controller
public class HomeController {

    private final BlogService blogService;
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    public HomeController(BlogService blogEntryService) {
        this.blogService = blogEntryService;
    }

    @GetMapping("/")
    public Object home(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest) {
        model.addAttribute("title", "Shawn Osborne's Website");
        model.addAttribute("metaDesc", "A software developer's personal portfolio and blogging website. By Shawn Osborne.");

        if (!htmxRequest.isHtmxRequest()) {
            model.addAttribute("fromAbout", true);
            return "index";
        }

        this.pushUrlIfNeeded(htmxRequest, htmxResponse, "/");

        return FragmentsRendering
                .fragment("components/shared-head::head-title")
                .fragment("index::about-main")
                .build();
    }

    // todo : get posts with 'portfolio' category and send to template,
    //  -- : portfolio card should have thumbnail, description, code_link, demo_link, and article_link (constructed with /blog/post/{id}/{slug}),
    //  -- : test

    @GetMapping("/portfolio")
    public Object portfolio(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest) {
        model.addAttribute("title", "Portfolio | Shawn Osborne");
        model.addAttribute("metaDesc", "A software developer's personal portfolio page. By Shawn Osborne.");
        model.addAttribute("entries", blogService.findAllSimplePortfolioEntries());

        if (!htmxRequest.isHtmxRequest()) {
            model.addAttribute("fromPortfolio", true);
            return "portfolio";
        }

        this.pushUrlIfNeeded(htmxRequest, htmxResponse, "/portfolio");

        return FragmentsRendering
                .fragment("components/shared-head::head-title")
                .fragment("portfolio::portfolio-main")
                .build();
    }

    private void pushUrlIfNeeded(HtmxRequest request, HtmxResponse response, String url) {
        if (request.getCurrentUrl() == null || !request.getCurrentUrl().endsWith(url)) {
            response.setPushUrl(url);
        }
    }
}
