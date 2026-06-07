package com.jeimandei.imanuelbytes.gateway.controller.admin;

import com.jeimandei.imanuelbytes.gateway.service.InteractionClientService;
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

@Controller
@RequestMapping("/admin/prayer-requests")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
public class AdminPrayerController {

    private static final Logger log = LoggerFactory.getLogger(AdminPrayerController.class);

    private final InteractionClientService interactionClientService;

    public AdminPrayerController(InteractionClientService interactionClientService) {
        this.interactionClientService = interactionClientService;
    }

    @GetMapping({"", "/"})
    public String listPrayerRequests(@RequestParam(defaultValue = "0") int page, Model model) {
        String jwt = SecurityUtils.getJwt();
        try {
            model.addAttribute("prayerRequests", interactionClientService.getPrayerRequests(page, 10, jwt));
        } catch (Exception e) {
            log.error("Failed to load prayer requests: {}", e.getMessage());
        }
        model.addAttribute("currentPage", page);
        return "admin/prayer-requests/list";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            interactionClientService.updatePrayerRequestStatus(id, status, jwt);
            redirectAttributes.addFlashAttribute("successMessage", "Prayer request status updated.");
        } catch (Exception e) {
            log.error("Failed to update prayer request {} status: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update status.");
        }
        return "redirect:/admin/prayer-requests";
    }
}
