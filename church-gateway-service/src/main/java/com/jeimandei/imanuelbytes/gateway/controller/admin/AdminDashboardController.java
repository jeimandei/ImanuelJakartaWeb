package com.jeimandei.imanuelbytes.gateway.controller.admin;

import com.jeimandei.imanuelbytes.gateway.service.EventClientService;
import com.jeimandei.imanuelbytes.gateway.service.InteractionClientService;
import com.jeimandei.imanuelbytes.gateway.service.SermonClientService;
import com.jeimandei.imanuelbytes.gateway.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
public class AdminDashboardController {

    private static final Logger log = LoggerFactory.getLogger(AdminDashboardController.class);

    private final EventClientService eventClientService;
    private final SermonClientService sermonClientService;
    private final InteractionClientService interactionClientService;

    public AdminDashboardController(EventClientService eventClientService,
                                    SermonClientService sermonClientService,
                                    InteractionClientService interactionClientService) {
        this.eventClientService = eventClientService;
        this.sermonClientService = sermonClientService;
        this.interactionClientService = interactionClientService;
    }

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        String jwt = SecurityUtils.getJwt();

        try {
            model.addAttribute("totalEvents",
                    eventClientService.getAllEvents(0, 1, jwt).getTotalElements());
        } catch (Exception e) {
            log.error("Dashboard: failed to load events count: {}", e.getMessage());
            model.addAttribute("totalEvents", 0);
        }

        try {
            model.addAttribute("totalSermons",
                    sermonClientService.getAllSermons(0, 1, jwt).getTotalElements());
        } catch (Exception e) {
            log.error("Dashboard: failed to load sermons count: {}", e.getMessage());
            model.addAttribute("totalSermons", 0);
        }

        try {
            var prayerPage = interactionClientService.getPrayerRequests(0, 5, jwt);
            model.addAttribute("prayerCount", prayerPage.getTotalElements());
            model.addAttribute("recentPrayerRequests", prayerPage.getContent());
        } catch (Exception e) {
            log.error("Dashboard: failed to load prayer requests: {}", e.getMessage());
            model.addAttribute("prayerCount", 0);
            model.addAttribute("recentPrayerRequests", Collections.emptyList());
        }

        try {
            var contactPage = interactionClientService.getContactMessages(0, 5, jwt);
            model.addAttribute("contactCount", contactPage.getTotalElements());
            model.addAttribute("recentContactMessages", contactPage.getContent());
        } catch (Exception e) {
            log.error("Dashboard: failed to load contact messages: {}", e.getMessage());
            model.addAttribute("contactCount", 0);
            model.addAttribute("recentContactMessages", Collections.emptyList());
        }

        log.debug("Admin dashboard loaded");
        return "admin/dashboard";
    }
}
