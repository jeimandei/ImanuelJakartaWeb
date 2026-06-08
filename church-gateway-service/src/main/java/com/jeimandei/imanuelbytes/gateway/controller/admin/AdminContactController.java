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
@RequestMapping("/admin/contacts")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminContactController {

    private static final Logger log = LoggerFactory.getLogger(AdminContactController.class);

    private final InteractionClientService interactionClientService;

    public AdminContactController(InteractionClientService interactionClientService) {
        this.interactionClientService = interactionClientService;
    }

    @GetMapping({"", "/"})
    public String listContacts(@RequestParam(defaultValue = "0") int page, Model model) {
        String jwt = SecurityUtils.getJwt();
        try {
            model.addAttribute("contacts", interactionClientService.getContactMessages(page, 10, jwt));
        } catch (Exception e) {
            log.error("Failed to load contact messages: {}", e.getMessage());
        }
        model.addAttribute("currentPage", page);
        return "admin/contacts/list";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            interactionClientService.updateContactMessageStatus(id, status, jwt);
            log.info("Contact message {} status updated to {}", id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Contact message status updated.");
        } catch (Exception e) {
            log.error("Failed to update contact message {} status: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update status.");
        }
        return "redirect:/admin/contacts";
    }
}
