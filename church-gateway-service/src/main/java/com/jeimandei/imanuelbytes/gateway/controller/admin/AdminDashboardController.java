package com.jeimandei.imanuelbytes.gateway.controller.admin;

import com.jeimandei.imanuelbytes.gateway.service.CmsClientService;
import com.jeimandei.imanuelbytes.gateway.service.EventClientService;
import com.jeimandei.imanuelbytes.gateway.service.InteractionClientService;
import com.jeimandei.imanuelbytes.gateway.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
public class AdminDashboardController {

    private static final Logger log = LoggerFactory.getLogger(AdminDashboardController.class);

    private final EventClientService eventClientService;
    private final CmsClientService cmsClientService;
    private final InteractionClientService interactionClientService;

    public AdminDashboardController(EventClientService eventClientService,
                                    CmsClientService cmsClientService,
                                    InteractionClientService interactionClientService) {
        this.eventClientService = eventClientService;
        this.cmsClientService = cmsClientService;
        this.interactionClientService = interactionClientService;
    }

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        String jwt = SecurityUtils.getJwt();

        try {
            model.addAttribute("upcomingEventsCount",
                    eventClientService.getUpcomingEvents().size());
        } catch (Exception e) {
            log.error("Dashboard: failed to load events count: {}", e.getMessage());
            model.addAttribute("upcomingEventsCount", 0);
        }

        try {
            model.addAttribute("announcementsCount",
                    cmsClientService.getActiveAnnouncements().size());
        } catch (Exception e) {
            log.error("Dashboard: failed to load announcements count: {}", e.getMessage());
            model.addAttribute("announcementsCount", 0);
        }

        try {
            model.addAttribute("prayerRequestsCount",
                    interactionClientService.getPrayerRequests(0, 1, jwt).getTotalElements());
        } catch (Exception e) {
            log.error("Dashboard: failed to load prayer requests count: {}", e.getMessage());
            model.addAttribute("prayerRequestsCount", 0);
        }

        log.debug("Admin dashboard loaded");
        return "admin/dashboard";
    }
}
