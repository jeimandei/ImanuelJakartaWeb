package com.jeimandei.imanuelbytes.gateway.controller.admin;

import com.jeimandei.imanuelbytes.gateway.dto.PermissionCategoryDto;
import com.jeimandei.imanuelbytes.gateway.dto.PermissionDto;
import com.jeimandei.imanuelbytes.gateway.service.RoleClientService;
import com.jeimandei.imanuelbytes.gateway.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/permissions")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminPermissionController {

    private static final Logger log = LoggerFactory.getLogger(AdminPermissionController.class);

    private static final List<String> CATEGORIES =
            List.of("USERS", "CONTENT", "EVENTS", "MEDIA", "PASTORAL", "COMMUNITY", "SETTINGS", "AUDIT");

    private final RoleClientService roleClientService;

    public AdminPermissionController(RoleClientService roleClientService) {
        this.roleClientService = roleClientService;
    }

    @GetMapping({"", "/"})
    public String listPermissions(Model model) {
        String jwt = SecurityUtils.getJwt();
        try {
            model.addAttribute("permissions", roleClientService.getAllPermissions(jwt));
        } catch (Exception e) {
            log.error("Failed to load permissions: {}", e.getMessage());
            model.addAttribute("permissions", Collections.emptyList());
        }
        model.addAttribute("categories", CATEGORIES);
        return "admin/permissions/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("permission", new PermissionDto());
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("isNew", true);
        return "admin/permissions/form";
    }

    @PostMapping
    public String createPermission(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> body = buildBody(request);
            roleClientService.createPermission(body, jwt);
            redirectAttributes.addFlashAttribute("successMessage", "Permission created successfully.");
        } catch (Exception e) {
            log.error("Failed to create permission: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create permission: " + e.getMessage());
        }
        return "redirect:/admin/permissions";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        String jwt = SecurityUtils.getJwt();
        PermissionDto permission = null;
        try {
            permission = roleClientService.getPermissionById(id, jwt);
        } catch (Exception e) {
            log.error("Failed to load permission {}: {}", id, e.getMessage());
        }
        model.addAttribute("permission", permission != null ? permission : new PermissionDto());
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("isNew", false);
        return "admin/permissions/form";
    }

    @PostMapping("/{id}/edit")
    public String updatePermission(@PathVariable Long id,
                                   HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("description", request.getParameter("description"));
            body.put("category", request.getParameter("category"));
            roleClientService.updatePermission(id, body, jwt);
            redirectAttributes.addFlashAttribute("successMessage", "Permission updated successfully.");
        } catch (Exception e) {
            log.error("Failed to update permission {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update permission: " + e.getMessage());
        }
        return "redirect:/admin/permissions";
    }

    @PostMapping("/{id}/delete")
    public String deletePermission(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            roleClientService.deletePermission(id, jwt);
            redirectAttributes.addFlashAttribute("successMessage", "Permission deleted successfully.");
        } catch (Exception e) {
            log.error("Failed to delete permission {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete permission: " + e.getMessage());
        }
        return "redirect:/admin/permissions";
    }

    private Map<String, Object> buildBody(HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        String name = request.getParameter("name");
        if (name != null && !name.isBlank()) {
            body.put("name", name.toUpperCase().trim());
        }
        body.put("description", request.getParameter("description"));
        body.put("category", request.getParameter("category"));
        return body;
    }
}
