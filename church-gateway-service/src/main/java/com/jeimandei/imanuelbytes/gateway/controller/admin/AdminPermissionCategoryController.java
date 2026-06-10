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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/permission-categories")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminPermissionCategoryController {

    private static final Logger log = LoggerFactory.getLogger(AdminPermissionCategoryController.class);

    private final RoleClientService roleClientService;

    public AdminPermissionCategoryController(RoleClientService roleClientService) {
        this.roleClientService = roleClientService;
    }

    @GetMapping({"", "/"})
    public String list(Model model) {
        String jwt = SecurityUtils.getJwt();
        try {
            model.addAttribute("categories", roleClientService.getAllCategories(jwt));
        } catch (Exception e) {
            log.error("Failed to load categories: {}", e.getMessage());
            model.addAttribute("categories", Collections.emptyList());
        }
        return "admin/permission-categories/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("category", new PermissionCategoryDto());
        model.addAttribute("isNew", true);
        return "admin/permission-categories/form";
    }

    @PostMapping
    public String create(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> body = new HashMap<>();
            String name = request.getParameter("name");
            if (name != null && !name.isBlank()) body.put("name", name.toUpperCase().trim());
            roleClientService.createCategory(body, jwt);
            redirectAttributes.addFlashAttribute("successMessage", "Category created successfully.");
        } catch (Exception e) {
            log.error("Failed to create category: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create category: " + e.getMessage());
        }
        return "redirect:/admin/permission-categories";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        String jwt = SecurityUtils.getJwt();
        PermissionCategoryDto cat = null;
        try {
            cat = roleClientService.getCategoryById(id, jwt);
        } catch (Exception e) {
            log.error("Failed to load category {}: {}", id, e.getMessage());
        }
        List<PermissionDto> allPermissions = Collections.emptyList();
        try {
            allPermissions = roleClientService.getAllPermissions(jwt);
        } catch (Exception e) {
            log.error("Failed to load permissions: {}", e.getMessage());
        }
        final String catName = cat != null ? cat.getName() : "";
        List<PermissionDto> inCategory = allPermissions.stream()
                .filter(p -> catName.equals(p.getCategory())).collect(Collectors.toList());
        List<PermissionDto> otherPermissions = allPermissions.stream()
                .filter(p -> !catName.equals(p.getCategory())).collect(Collectors.toList());
        model.addAttribute("category", cat != null ? cat : new PermissionCategoryDto());
        model.addAttribute("inCategory", inCategory);
        model.addAttribute("otherPermissions", otherPermissions);
        model.addAttribute("isNew", false);
        return "admin/permission-categories/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> nameBody = new HashMap<>();
            String newName = request.getParameter("name");
            if (newName != null && !newName.isBlank()) nameBody.put("name", newName.toUpperCase().trim());
            PermissionCategoryDto updated = roleClientService.updateCategory(id, nameBody, jwt);
            String categoryName = updated != null && updated.getName() != null
                    ? updated.getName() : (newName != null ? newName.toUpperCase().trim() : "");

            String[] checked = request.getParameterValues("permissionIds");
            int moved = 0;
            if (checked != null && checked.length > 0) {
                List<Long> checkedIds = Arrays.stream(checked)
                        .map(Long::parseLong).collect(Collectors.toList());
                for (Long permId : checkedIds) {
                    roleClientService.updatePermission(permId, Map.of("category", categoryName), jwt);
                    moved++;
                }
            }
            String msg = "Category updated successfully.";
            if (moved > 0) msg += " " + moved + " permission(s) assigned.";
            redirectAttributes.addFlashAttribute("successMessage", msg);
        } catch (Exception e) {
            log.error("Failed to update category {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update: " + e.getMessage());
        }
        return "redirect:/admin/permission-categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            roleClientService.deleteCategory(id, jwt);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully.");
        } catch (Exception e) {
            log.error("Failed to delete category {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete category: " + e.getMessage());
        }
        return "redirect:/admin/permission-categories";
    }
}
