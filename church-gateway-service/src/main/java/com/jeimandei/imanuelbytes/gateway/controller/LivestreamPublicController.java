package com.jeimandei.imanuelbytes.gateway.controller;

import com.jeimandei.imanuelbytes.gateway.service.LivestreamClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/livestream")
public class LivestreamPublicController {

    private static final Logger log = LoggerFactory.getLogger(LivestreamPublicController.class);

    private final LivestreamClientService livestreamClientService;

    public LivestreamPublicController(LivestreamClientService livestreamClientService) {
        this.livestreamClientService = livestreamClientService;
    }

    @GetMapping
    public String livestream(Model model) {
        try {
            var active = livestreamClientService.getActiveLivestream().orElse(null);
            model.addAttribute("activeLivestream", active);
            log.debug("Active livestream loaded: {}", active != null ? "found" : "not found");
        } catch (Exception e) {
            log.error("Failed to load active livestream: {}", e.getMessage());
        }
        return "public/livestream";
    }
}
