package dev.bnacar.distributedratelimiter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to forward SPA React routes to index.html so direct URLs and page refreshes work.
 */
@Controller
public class SpaController {

    @GetMapping(value = {
        "/",
        "/algorithms",
        "/configuration",
        "/scheduling",
        "/load-testing",
        "/analytics",
        "/api-keys",
        "/adaptive"
    })
    public String forwardSpaRoutes() {
        return "forward:/index.html";
    }
}
