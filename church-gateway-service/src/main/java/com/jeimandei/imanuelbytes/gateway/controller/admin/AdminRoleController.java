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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/roles")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminRoleController {

    private static final Logger log = LoggerFactory.getLogger(AdminRoleController.class);
    private static final List<String> ACTION_COLUMNS = List.of("VIEW", "CREATE", "EDIT", "DELETE", "OTHERS");

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
        model.addAttribute("permMatrix", buildPermMatrix(permissions));
        model.addAttribute("actionColumns", ACTION_COLUMNS);
        model.addAttribute("rolePermIds", Collections.emptySet());
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
        RoleDto role = new RoleDto();
        try {
            role = roleClientService.getRoleById(id, jwt);
        } catch (Exception e) {
            log.error("Failed to load role {}: {}", id, e.getMessage());
            model.addAttribute("role", new RoleDto());
        }
        try {
            model.addAttribute("permissions", roleClientService.getAllPermissions(jwt));
        } catch (Exception e) {
            log.error("Failed to load permissions: {}", e.getMessage());
            model.addAttribute("permissions", Collections.emptyList());
        }
        model.addAttribute("role", role);
        Set<Long> rolePermIds = role != null && role.getPermissions() != null
                ? role.getPermissions().stream().map(PermissionDto::getId).collect(Collectors.toSet())
                : Collections.emptySet();
        model.addAttribute("rolePermIds", rolePermIds);
        List<PermissionDto> permissions = Collections.emptyList();
        try {
            permissions = roleClientService.getAllPermissions(jwt);
        } catch (Exception e) {
            log.error("Failed to load permissions: {}", e.getMessage());
        }
        model.addAttribute("permMatrix", buildPermMatrix(permissions));
        model.addAttribute("actionColumns", ACTION_COLUMNS);
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
            redirectAttributes.addFlashAttribute("successMessage", "Role updated successfully.");
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

    // category → subject → action → List<PermissionDto>
    private Map<String, Map<String, Map<String, List<PermissionDto>>>> buildPermMatrix(List<PermissionDto> permissions) {
        // Collect sorted distinct categories
        List<String> categories = permissions.stream()
                .map(PermissionDto::getCategory)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        Map<String, Map<String, Map<String, List<PermissionDto>>>> matrix = new LinkedHashMap<>();
        for (String cat : categories) {
            Map<String, Map<String, List<PermissionDto>>> catMap = new LinkedHashMap<>();
            for (PermissionDto perm : permissions) {
                if (!cat.equals(perm.getCategory())) continue;
                String action = extractAction(perm.getName());
                String subject = extractSubject(perm.getName());
                catMap.computeIfAbsent(subject, k -> new LinkedHashMap<>())
                      .computeIfAbsent(action, k -> new ArrayList<>())
                      .add(perm);
            }
            if (!catMap.isEmpty()) matrix.put(cat, catMap);
        }
        // Catch uncategorised permissions so they are never silently hidden
        List<PermissionDto> uncategorized = permissions.stream()
                .filter(p -> p.getCategory() == null || p.getCategory().isBlank())
                .collect(Collectors.toList());
        if (!uncategorized.isEmpty()) {
            Map<String, Map<String, List<PermissionDto>>> catMap = new LinkedHashMap<>();
            for (PermissionDto perm : uncategorized) {
                catMap.computeIfAbsent(extractSubject(perm.getName()), k -> new LinkedHashMap<>())
                      .computeIfAbsent(extractAction(perm.getName()), k -> new ArrayList<>())
                      .add(perm);
            }
            matrix.put("(uncategorized)", catMap);
        }
        return matrix;
    }

    private String extractAction(String name) {
        if (name.endsWith("_VIEW"))   return "VIEW";
        if (name.endsWith("_CREATE")) return "CREATE";
        if (name.endsWith("_EDIT"))   return "EDIT";
        if (name.endsWith("_DELETE")) return "DELETE";
        return "OTHERS";
    }

    private String extractSubject(String name) {
        for (String suffix : new String[]{"_VIEW", "_CREATE", "_EDIT", "_DELETE",
                                          "_MANAGE", "_PUBLISH", "_FEATURED"}) {
            if (name.endsWith(suffix)) return name.substring(0, name.length() - suffix.length());
        }
        return name;
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
