package com.jeimandei.imanuelbytes.gateway.controller.admin;

import com.jeimandei.imanuelbytes.gateway.dto.UserDto;
import com.jeimandei.imanuelbytes.gateway.service.UserClientService;
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
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminUserController {

    private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);

    private final UserClientService userClientService;

    public AdminUserController(UserClientService userClientService) {
        this.userClientService = userClientService;
    }

    @GetMapping({"", "/"})
    public String listUsers(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(required = false) String search,
                            Model model) {
        String jwt = SecurityUtils.getJwt();
        try {
            model.addAttribute("users", userClientService.getAllUsers(jwt));
        } catch (Exception e) {
            log.error("Failed to load users: {}", e.getMessage());
        }
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        return "admin/users/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("userForm", new HashMap<String, Object>());
        return "admin/users/create";
    }

    @PostMapping
    public String createUser(@RequestParam Map<String, String> params,
                             RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> request = new HashMap<>(params);
            userClientService.createUser(request, jwt);
            log.info("User created successfully");
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully.");
        } catch (Exception e) {
            log.error("Failed to create user: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        String jwt = SecurityUtils.getJwt();
        try {
            UserDto user = userClientService.getUserById(id, jwt);
            model.addAttribute("user", user);
        } catch (Exception e) {
            log.error("Failed to load user {}: {}", id, e.getMessage());
        }
        return "admin/users/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Long id,
                             @RequestParam Map<String, String> params,
                             RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> request = new HashMap<>(params);
            userClientService.updateUser(id, request, jwt);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully.");
        } catch (Exception e) {
            log.error("Failed to update user {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            userClientService.updateUserStatus(id, status, jwt);
            log.info("User {} status updated to {}", id, status);
            redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully.");
        } catch (Exception e) {
            log.error("Failed to update status for user {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update user status.");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/roles")
    public String assignRoles(@PathVariable Long id,
                              @RequestParam List<String> roles,
                              RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            userClientService.assignRoles(id, roles, jwt);
            log.info("Roles assigned to user {}: {}", id, roles);
            redirectAttributes.addFlashAttribute("successMessage", "User roles updated successfully.");
        } catch (Exception e) {
            log.error("Failed to assign roles for user {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update user roles.");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            userClientService.deleteUser(id, jwt);
            log.info("User {} deleted successfully", id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
        } catch (Exception e) {
            log.error("Failed to delete user {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete user.");
        }
        return "redirect:/admin/users";
    }
}
