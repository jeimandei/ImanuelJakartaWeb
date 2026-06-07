package com.jeimandei.imanuelbytes.gateway.controller.admin;

import com.jeimandei.imanuelbytes.gateway.service.CmsClientService;
import com.jeimandei.imanuelbytes.gateway.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/settings")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminSettingsController {

    private static final Logger log = LoggerFactory.getLogger(AdminSettingsController.class);

    private final CmsClientService cmsClientService;

    public AdminSettingsController(CmsClientService cmsClientService) {
        this.cmsClientService = cmsClientService;
    }

    @GetMapping({"", "/"})
    public String settings(Model model) {
        String jwt = SecurityUtils.getJwt();
        try {
            model.addAttribute("settings", cmsClientService.getAllSettings(jwt));
        } catch (Exception e) {
            log.error("Failed to load settings: {}", e.getMessage());
        }
        return "admin/settings/index";
    }

    @PostMapping("/update")
    public String updateSettings(@RequestParam Map<String, String> settings,
                                 RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        int successCount = 0;
        int failCount = 0;
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            try {
                cmsClientService.updateSetting(entry.getKey(), entry.getValue(), jwt);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to update setting {}: {}", entry.getKey(), e.getMessage());
                failCount++;
            }
        }
        if (failCount == 0) {
            redirectAttributes.addFlashAttribute("successMessage",
                    successCount + " setting(s) updated successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    failCount + " setting(s) failed to update.");
        }
        return "redirect:/admin/settings";
    }
}
