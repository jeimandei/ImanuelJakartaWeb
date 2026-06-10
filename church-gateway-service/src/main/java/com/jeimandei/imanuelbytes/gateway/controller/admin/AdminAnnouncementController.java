package com.jeimandei.imanuelbytes.gateway.controller.admin;

import com.jeimandei.imanuelbytes.gateway.dto.AnnouncementDto;
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
    public String createForm(Model model) {
        model.addAttribute("announcementForm", new AnnouncementDto());
        return "admin/announcements/form";
    }

    @PostMapping("/create")
    public String createAnnouncement(@RequestParam Map<String, String> params,
                                     RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> request = buildAnnouncementBody(params);
            cmsClientService.createAnnouncement(request, jwt);
            log.info("Announcement created successfully");
            redirectAttributes.addFlashAttribute("successMessage", "Announcement created successfully.");
        } catch (Exception e) {
            log.error("Failed to create announcement: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create announcement.");
        }
        return "redirect:/admin/announcements";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            AnnouncementDto announcement = cmsClientService.getAnnouncementById(id, jwt);
            if (announcement == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Announcement not found.");
                return "redirect:/admin/announcements";
            }
            model.addAttribute("announcementForm", announcement);
        } catch (Exception e) {
            log.error("Failed to load announcement {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to load announcement.");
            return "redirect:/admin/announcements";
        }
        return "admin/announcements/form";
    }

    @PostMapping("/{id}/edit")
    public String updateAnnouncement(@PathVariable Long id,
                                     @RequestParam Map<String, String> params,
                                     RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> request = buildAnnouncementBody(params);
            cmsClientService.updateAnnouncement(id, request, jwt);
            log.info("Announcement {} updated successfully", id);
            redirectAttributes.addFlashAttribute("successMessage", "Announcement updated successfully.");
        } catch (Exception e) {
            log.error("Failed to update announcement {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update announcement.");
        }
        return "redirect:/admin/announcements";
    }

    private Map<String, Object> buildAnnouncementBody(Map<String, String> params) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", params.get("title"));
        body.put("message", params.get("message"));
        String startDate = params.get("startDate");
        if (startDate != null && !startDate.isBlank()) body.put("startDate", startDate);
        String endDate = params.get("endDate");
        if (endDate != null && !endDate.isBlank()) body.put("endDate", endDate);
        String priority = params.get("priority");
        if (priority != null && !priority.isBlank()) body.put("priority", Integer.parseInt(priority));
        // Thymeleaf adds a hidden field so active is always present: "true" when checked, "false" when not
        body.put("active", "true".equalsIgnoreCase(params.get("active")));
        return body;
    }

    @PostMapping("/{id}/delete")
    public String deleteAnnouncement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            cmsClientService.deleteAnnouncement(id, jwt);
            log.info("Announcement {} deleted successfully", id);
            redirectAttributes.addFlashAttribute("successMessage", "Announcement deleted successfully.");
        } catch (Exception e) {
            log.error("Failed to delete announcement {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete announcement.");
        }
        return "redirect:/admin/announcements";
    }
}
