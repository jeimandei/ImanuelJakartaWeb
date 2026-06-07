package com.jeimandei.imanuelbytes.gateway.controller.admin;

import com.jeimandei.imanuelbytes.gateway.service.LivestreamClientService;
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
@RequestMapping("/admin/livestreams")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminLivestreamController {

    private static final Logger log = LoggerFactory.getLogger(AdminLivestreamController.class);

    private final LivestreamClientService livestreamClientService;

    public AdminLivestreamController(LivestreamClientService livestreamClientService) {
        this.livestreamClientService = livestreamClientService;
    }

    @GetMapping({"", "/"})
    public String listLivestreams(@RequestParam(defaultValue = "0") int page, Model model) {
        String jwt = SecurityUtils.getJwt();
        try {
            model.addAttribute("livestreams", livestreamClientService.getAllLivestreams(page, 10, jwt));
        } catch (Exception e) {
            log.error("Failed to load livestreams: {}", e.getMessage());
        }
        model.addAttribute("currentPage", page);
        return "admin/livestreams/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("livestreamForm", new HashMap<String, Object>());
        return "admin/livestreams/form";
    }

    @PostMapping("/create")
    public String createLivestream(@RequestParam Map<String, String> params,
                                   RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> request = new HashMap<>(params);
            livestreamClientService.createLivestream(request, jwt);
            redirectAttributes.addFlashAttribute("successMessage", "Livestream created successfully.");
        } catch (Exception e) {
            log.error("Failed to create livestream: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create livestream.");
        }
        return "redirect:/admin/livestreams";
    }

    @PostMapping("/{id}/activate")
    public String activateLivestream(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            livestreamClientService.activateLivestream(id, jwt);
            redirectAttributes.addFlashAttribute("successMessage", "Livestream activated.");
        } catch (Exception e) {
            log.error("Failed to activate livestream {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to activate livestream.");
        }
        return "redirect:/admin/livestreams";
    }

    @PostMapping("/{id}/delete")
    public String deleteLivestream(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            livestreamClientService.deleteLivestream(id, jwt);
            redirectAttributes.addFlashAttribute("successMessage", "Livestream deleted successfully.");
        } catch (Exception e) {
            log.error("Failed to delete livestream {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete livestream.");
        }
        return "redirect:/admin/livestreams";
    }
}
