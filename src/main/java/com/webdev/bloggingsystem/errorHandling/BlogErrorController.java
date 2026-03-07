package com.webdev.bloggingsystem.errorHandling;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.FragmentsRendering;

@Controller
public class BlogErrorController implements ErrorController {
    private static final Logger logger = LoggerFactory.getLogger(BlogErrorController.class);

    // todo : error pages / fragments for different status codes

    @RequestMapping("/error")
    public FragmentsRendering handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        logger.info(status.toString());
        if ("true".equals(request.getHeader("HX-Request"))) {
            return FragmentsRendering
                    .fragment("error/error::error-field")
                    .header("HX-Retarget", "#error-field")  // override hx-target
                    .header("HX-Reswap", "outerHTML")
                    .build();
        }

        return FragmentsRendering.fragment("error/error").build();
    }
}
