package com.jeimandei.imanuelbytes.gateway.controller.admin;

import com.jeimandei.imanuelbytes.gateway.dto.PermissionDto;
import com.jeimandei.imanuelbytes.gateway.dto.RoleDto;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/roles")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminRoleController {

    private static final Logger log = LoggerFactory.getLogger(AdminRoleController.class);

    private final RoleClientService roleClientService;

    public AdminRoleController(RoleClientService roleClientService) {
        this.roleClientService = roleClientService;
    }

    @GetMapping({"", "/"})
    public String listRoles(Model model) {
        String jwt = SecurityUtils.getJwt();
        try {
            model.addAttribute("roles", roleClientService.getAllRoles(jwt));
        } catch (Exception e) {
            log.error("Failed to load roles: {}", e.getMessage());
            model.addAttribute("roles", Collections.emptyList());
        }
        return "admin/roles/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        String jwt = SecurityUtils.getJwt();
        List<PermissionDto> permissions = Collections.emptyList();
        try {
            permissions = roleClientService.getAllPermissions(jwt);
        } catch (Exception e) {
            log.error("Failed to load permissions: {}", e.getMessage());
        }
        model.addAttribute("permissions", permissions);
        model.addAttribute("categories", deriveCategories(permissions));
        model.addAttribute("role", new RoleDto());
        model.addAttribute("isNew", true);
        return "admin/roles/form";
    }

    @PostMapping
    public String createRole(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> body = buildRoleRequestBody(request);
            roleClientService.createRole(body, jwt);
            redirectAttributes.addFlashAttribute("successMessage", "Role created successfully.");
        } catch (Exception e) {
            log.error("Failed to create role: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create role: " + e.getMessage());
        }
        return "redirect:/admin/roles";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        String jwt = SecurityUtils.getJwt();
        try {
            model.addAttribute("role", roleClientService.getRoleById(id, jwt));
        } catch (Exception e) {
            log.error("Failed to load role {}: {}", id, e.getMessage());
            model.addAttribute("role", new RoleDto());
        }
        List<PermissionDto> permissions = Collections.emptyList();
        try {
            permissions = roleClientService.getAllPermissions(jwt);
        } catch (Exception e) {
            log.error("Failed to load permissions: {}", e.getMessage());
        }
        model.addAttribute("permissions", permissions);
        model.addAttribute("categories", deriveCategories(permissions));
        model.addAttribute("isNew", false);
        return "admin/roles/form";
    }

    @PostMapping("/{id}/edit")
    public String updateRole(@PathVariable Long id,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> body = buildRoleRequestBody(request);
            body.remove("roleName"); // name cannot be changed on update
            roleClientService.updateRole(id, body, jwt);

            int catChanged = applyPermissionCategoryChanges(request, jwt);
            String msg = "Role updated successfully.";
            if (catChanged > 0) msg += " " + catChanged + " permission categor" + (catChanged == 1 ? "y" : "ies") + " reorganized.";
            redirectAttributes.addFlashAttribute("successMessage", msg);
        } catch (Exception e) {
            log.error("Failed to update role {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update role: " + e.getMessage());
        }
        return "redirect:/admin/roles";
    }

    @PostMapping("/{id}/delete")
    public String deleteRole(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            roleClientService.deleteRole(id, jwt);
            redirectAttributes.addFlashAttribute("successMessage", "Role deleted successfully.");
        } catch (Exception e) {
            log.error("Failed to delete role {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete role: " + e.getMessage());
        }
        return "redirect:/admin/roles";
    }

    private int applyPermissionCategoryChanges(HttpServletRequest request, String jwt) {
        List<PermissionDto> allPerms = Collections.emptyList();
        try { allPerms = roleClientService.getAllPermissions(jwt); } catch (Exception ignored) {}
        int changed = 0;
        for (PermissionDto perm : allPerms) {
            String newCat = request.getParameter("permCat_" + perm.getId());
            String currentCat = perm.getCategory() != null ? perm.getCategory() : "";
            if (newCat != null && !newCat.equals(currentCat)) {
                try {
                    roleClientService.updatePermission(perm.getId(), Map.of("category", newCat), jwt);
                    changed++;
                } catch (Exception e) {
                    log.warn("Failed to update category for permission {}: {}", perm.getId(), e.getMessage());
                }
            }
        }
        return changed;
    }

    private List<String> deriveCategories(List<PermissionDto> permissions) {
        return permissions.stream()
                .map(PermissionDto::getCategory)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildRoleRequestBody(HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        String roleName = request.getParameter("roleName");
        if (roleName != null && !roleName.isBlank()) {
            body.put("roleName", roleName.toUpperCase().startsWith("ROLE_")
                    ? roleName.toUpperCase()
                    : "ROLE_" + roleName.toUpperCase());
        }
        body.put("description", request.getParameter("description"));
        String[] permIds = request.getParameterValues("permissionIds");
        List<Long> ids = permIds != null
                ? Arrays.stream(permIds).map(Long::parseLong).toList()
                : Collections.emptyList();
        body.put("permissionIds", ids);
        return body;
    }
}
