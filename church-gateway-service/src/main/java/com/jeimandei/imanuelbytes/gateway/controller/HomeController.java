package com.jeimandei.imanuelbytes.gateway.controller;

import com.jeimandei.imanuelbytes.gateway.service.CmsClientService;
import com.jeimandei.imanuelbytes.gateway.service.EventClientService;
import com.jeimandei.imanuelbytes.gateway.service.LivestreamClientService;
import com.jeimandei.imanuelbytes.gateway.service.SermonClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    private final EventClientService eventClientService;
    private final SermonClientService sermonClientService;
    private final CmsClientService cmsClientService;
    private final LivestreamClientService livestreamClientService;

    public HomeController(EventClientService eventClientService,
                          SermonClientService sermonClientService,
                          CmsClientService cmsClientService,
                          LivestreamClientService livestreamClientService) {
        this.eventClientService = eventClientService;
        this.sermonClientService = sermonClientService;
        this.cmsClientService = cmsClientService;
        this.livestreamClientService = livestreamClientService;
    }

    @GetMapping("/")
    public String home(Model model) {
        try {
            model.addAttribute("featuredEvents", eventClientService.getFeaturedEvents());
        } catch (Exception e) {
            log.error("Failed to load featured events for home: {}", e.getMessage());
        }
        try {
            model.addAttribute("latestSermons", sermonClientService.getLatestSermons());
        } catch (Exception e) {
            log.error("Failed to load latest sermons for home: {}", e.getMessage());
        }
        try {
            model.addAttribute("announcements", cmsClientService.getActiveAnnouncements());
        } catch (Exception e) {
            log.error("Failed to load announcements for home: {}", e.getMessage());
        }
        try {
            model.addAttribute("activeLivestream", livestreamClientService.getActiveLivestream().orElse(null));
        } catch (Exception e) {
            log.error("Failed to load active livestream for home: {}", e.getMessage());
        }
        return "public/home";
    }

    @GetMapping("/about")
    public String about() {
        return "public/about";
    }

    @GetMapping("/services")
    public String services() {
        return "public/services";
    }

    @GetMapping("/ministries")
    public String ministries() {
        return "public/ministries";
    }

    @GetMapping("/giving")
    public String giving() {
        return "public/giving";
    }

    @GetMapping("/new-here")
    public String newHere() {
        return "public/new-here";
    }

    @GetMapping("/faq")
    public String faq() {
        return "public/faq";
    }

    @GetMapping("/leadership")
    public String leadership() {
        return "public/leadership";
    }

    @GetMapping("/403")
    public String forbidden() {
        return "auth/403";
    }
}
