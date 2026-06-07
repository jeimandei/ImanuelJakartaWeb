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
@RequestMapping("/admin/cms")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
public class AdminCmsController {

    private static final Logger log = LoggerFactory.getLogger(AdminCmsController.class);

    private final CmsClientService cmsClientService;

    public AdminCmsController(CmsClientService cmsClientService) {
        this.cmsClientService = cmsClientService;
    }

    @GetMapping({"", "/"})
    public String listPages(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(required = false) String q,
                            Model model) {
        String jwt = SecurityUtils.getJwt();
        try {
            model.addAttribute("pages", cmsClientService.getAllCmsPages(page, 10, jwt));
        } catch (Exception e) {
            log.error("Failed to load CMS pages: {}", e.getMessage());
        }
        model.addAttribute("currentPage", page);
        model.addAttribute("q", q);
        return "admin/cms/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("cmsForm", new HashMap<String, Object>());
        return "admin/cms/form";
    }

    @PostMapping({"/create", ""})
    public String createPage(@RequestParam Map<String, String> params,
                             RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> request = new HashMap<>(params);
            cmsClientService.createCmsPage(request, jwt);
            log.info("CMS page created successfully");
            redirectAttributes.addFlashAttribute("successMessage", "CMS page created successfully.");
        } catch (Exception e) {
            log.error("Failed to create CMS page: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create CMS page: " + e.getMessage());
        }
        return "redirect:/admin/cms";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        String jwt = SecurityUtils.getJwt();
        try {
            model.addAttribute("cmsForm", cmsClientService.getCmsPageById(id, jwt));
        } catch (Exception e) {
            log.error("Failed to load CMS page {}: {}", id, e.getMessage());
        }
        return "admin/cms/form";
    }

    @PostMapping("/{id}/edit")
    public String updatePage(@PathVariable Long id,
                             @RequestParam Map<String, String> params,
                             RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> request = new HashMap<>(params);
            cmsClientService.updateCmsPage(id, request, jwt);
            log.info("CMS page {} updated successfully", id);
            redirectAttributes.addFlashAttribute("successMessage", "CMS page updated successfully.");
        } catch (Exception e) {
            log.error("Failed to update CMS page {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update CMS page.");
        }
        return "redirect:/admin/cms";
    }

    @PostMapping("/{id}/publish")
    public String publishPage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            cmsClientService.publishCmsPage(id, jwt);
            log.info("CMS page {} published successfully", id);
            redirectAttributes.addFlashAttribute("successMessage", "Page published successfully.");
        } catch (Exception e) {
            log.error("Failed to publish CMS page {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to publish page.");
        }
        return "redirect:/admin/cms";
    }

    @PostMapping("/{id}/unpublish")
    public String unpublishPage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            cmsClientService.unpublishCmsPage(id, jwt);
            log.info("CMS page {} unpublished", id);
            redirectAttributes.addFlashAttribute("successMessage", "Page unpublished.");
        } catch (Exception e) {
            log.error("Failed to unpublish CMS page {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to unpublish page.");
        }
        return "redirect:/admin/cms";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public String deletePage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            cmsClientService.deleteCmsPage(id, jwt);
            log.info("CMS page {} deleted successfully", id);
            redirectAttributes.addFlashAttribute("successMessage", "Page deleted successfully.");
        } catch (Exception e) {
            log.error("Failed to delete CMS page {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete page.");
        }
        return "redirect:/admin/cms";
    }
}
