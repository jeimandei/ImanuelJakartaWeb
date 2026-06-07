package com.jeimandei.imanuelbytes.gateway.controller.admin;

import com.jeimandei.imanuelbytes.gateway.service.CmsClientService;
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
@RequestMapping("/admin/announcements")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
public class AdminAnnouncementController {

    private static final Logger log = LoggerFactory.getLogger(AdminAnnouncementController.class);

    private final CmsClientService cmsClientService;

    public AdminAnnouncementController(CmsClientService cmsClientService) {
        this.cmsClientService = cmsClientService;
    }

    @GetMapping({"", "/"})
    public String listAnnouncements(@RequestParam(defaultValue = "0") int page, Model model) {
        String jwt = SecurityUtils.getJwt();
        try {
            model.addAttribute("announcements", cmsClientService.getAllAnnouncements(page, 10, jwt));
        } catch (Exception e) {
            log.error("Failed to load announcements: {}", e.getMessage());
        }
        model.addAttribute("currentPage", page);
        return "admin/announcements/list";
    }

    @GetMapping("/create")
    public String createForm() {
        return "admin/announcements/form";
    }

    @PostMapping("/create")
    public String createAnnouncement(@RequestParam Map<String, String> params,
                                     RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> request = new HashMap<>(params);
            cmsClientService.createAnnouncement(request, jwt);
            redirectAttributes.addFlashAttribute("successMessage", "Announcement created successfully.");
        } catch (Exception e) {
            log.error("Failed to create announcement: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create announcement.");
        }
        return "redirect:/admin/announcements";
    }

    @PostMapping("/{id}/delete")
    public String deleteAnnouncement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            cmsClientService.deleteAnnouncement(id, jwt);
            redirectAttributes.addFlashAttribute("successMessage", "Announcement deleted successfully.");
        } catch (Exception e) {
            log.error("Failed to delete announcement {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete announcement.");
        }
        return "redirect:/admin/announcements";
    }
}
