package com.jeimandei.imanuelbytes.gateway.controller.admin;

import com.jeimandei.imanuelbytes.gateway.dto.SermonDto;
import com.jeimandei.imanuelbytes.gateway.service.SermonClientService;
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
@RequestMapping("/admin/sermons")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
public class AdminSermonController {

    private static final Logger log = LoggerFactory.getLogger(AdminSermonController.class);

    private final SermonClientService sermonClientService;

    public AdminSermonController(SermonClientService sermonClientService) {
        this.sermonClientService = sermonClientService;
    }

    @GetMapping({"", "/"})
    public String listSermons(@RequestParam(defaultValue = "0") int page, Model model) {
        String jwt = SecurityUtils.getJwt();
        try {
            model.addAttribute("sermons", sermonClientService.getAllSermons(page, 10, jwt));
        } catch (Exception e) {
            log.error("Failed to load sermons: {}", e.getMessage());
        }
        model.addAttribute("currentPage", page);
        return "admin/sermons/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("sermonForm", new SermonDto());
        return "admin/sermons/form";
    }

    @PostMapping("/create")
    public String createSermon(@RequestParam Map<String, String> params,
                               RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> request = new HashMap<>(params);
            sermonClientService.createSermon(request, jwt);
            log.info("Sermon created successfully");
            redirectAttributes.addFlashAttribute("successMessage", "Sermon created successfully.");
        } catch (Exception e) {
            log.error("Failed to create sermon: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create sermon.");
        }
        return "redirect:/admin/sermons";
    }

    @PostMapping("/{id}/delete")
    public String deleteSermon(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            sermonClientService.deleteSermon(id, jwt);
            log.info("Sermon {} deleted successfully", id);
            redirectAttributes.addFlashAttribute("successMessage", "Sermon deleted successfully.");
        } catch (Exception e) {
            log.error("Failed to delete sermon {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete sermon.");
        }
        return "redirect:/admin/sermons";
    }
}
