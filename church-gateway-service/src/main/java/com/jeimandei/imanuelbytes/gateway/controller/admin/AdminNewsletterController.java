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
@RequestMapping("/admin/newsletter")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminNewsletterController {

    private static final Logger log = LoggerFactory.getLogger(AdminNewsletterController.class);

    private final InteractionClientService interactionClientService;

    public AdminNewsletterController(InteractionClientService interactionClientService) {
        this.interactionClientService = interactionClientService;
    }

    @GetMapping({"", "/"})
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        String jwt = SecurityUtils.getJwt();
        try {
            model.addAttribute("subscriptions",
                    interactionClientService.getNewsletterSubscriptions(page, 20, jwt));
        } catch (Exception e) {
            log.error("Failed to load newsletter subscriptions: {}", e.getMessage());
            model.addAttribute("subscriptions",
                    com.jeimandei.imanuelbytes.gateway.dto.PageResponse.empty());
        }
        model.addAttribute("currentPage", page);
        return "admin/newsletter/list";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            interactionClientService.deleteNewsletterSubscription(id, jwt);
            log.info("Newsletter subscriber {} deleted", id);
            redirectAttributes.addFlashAttribute("successMessage", "Subscriber removed successfully.");
        } catch (Exception e) {
            log.error("Failed to delete newsletter subscriber {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove subscriber.");
        }
        return "redirect:/admin/newsletter";
    }
}
