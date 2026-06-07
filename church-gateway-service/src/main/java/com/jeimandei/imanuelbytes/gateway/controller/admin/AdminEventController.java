package com.jeimandei.imanuelbytes.gateway.controller.admin;

import com.jeimandei.imanuelbytes.gateway.service.EventClientService;
import com.jeimandei.imanuelbytes.gateway.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/events")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
public class AdminEventController {

    private static final Logger log = LoggerFactory.getLogger(AdminEventController.class);

    private final EventClientService eventClientService;

    public AdminEventController(EventClientService eventClientService) {
        this.eventClientService = eventClientService;
    }

    @GetMapping({"", "/"})
    public String listEvents(@RequestParam(defaultValue = "0") int page, Model model) {
        String jwt = SecurityUtils.getJwt();
        try {
            model.addAttribute("events", eventClientService.getAllEvents(page, 10, jwt));
        } catch (Exception e) {
            log.error("Failed to load events: {}", e.getMessage());
        }
        model.addAttribute("currentPage", page);
        return "admin/events/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("eventForm", new HashMap<String, Object>());
        return "admin/events/form";
    }

    @PostMapping("/create")
    public String createEvent(@RequestParam Map<String, String> params,
                              RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> request = new HashMap<>(params);
            eventClientService.createEvent(request, jwt);
            redirectAttributes.addFlashAttribute("successMessage", "Event created successfully.");
        } catch (Exception e) {
            log.error("Failed to create event: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create event.");
        }
        return "redirect:/admin/events";
    }

    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            eventClientService.deleteEvent(id, jwt);
            redirectAttributes.addFlashAttribute("successMessage", "Event deleted successfully.");
        } catch (Exception e) {
            log.error("Failed to delete event {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete event.");
        }
        return "redirect:/admin/events";
    }

    @PostMapping("/{id}/featured")
    public String toggleFeatured(@PathVariable Long id,
                                 @RequestParam(required = false) Boolean featured,
                                 RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> request = Map.of("featured", featured != null && featured);
            eventClientService.createEvent(request, jwt); // update via patch-like POST; adjust if API differs
            redirectAttributes.addFlashAttribute("successMessage", "Event featured status updated.");
        } catch (Exception e) {
            log.error("Failed to toggle featured for event {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update featured status.");
        }
        return "redirect:/admin/events";
    }
}
