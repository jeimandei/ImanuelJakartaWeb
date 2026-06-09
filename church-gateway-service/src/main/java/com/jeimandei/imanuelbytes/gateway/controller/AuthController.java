package com.jeimandei.imanuelbytes.gateway.controller;

import com.jeimandei.imanuelbytes.gateway.dto.LoginFormDto;
import com.jeimandei.imanuelbytes.gateway.dto.ProfileFormDto;
import com.jeimandei.imanuelbytes.gateway.dto.RegisterFormDto;
import com.jeimandei.imanuelbytes.gateway.security.GatewayUserDetails;
import com.jeimandei.imanuelbytes.gateway.service.AuthClientService;
import com.jeimandei.imanuelbytes.gateway.util.SecurityUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthClientService authClientService;
    private final AuthenticationManager authenticationManager;

    public AuthController(AuthClientService authClientService,
                          AuthenticationManager authenticationManager) {
        this.authClientService = authClientService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        if (SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                && SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                instanceof GatewayUserDetails) {
            return "redirect:/";
        }
        model.addAttribute("loginForm", new LoginFormDto());
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerForm", new RegisterFormDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid RegisterFormDto form,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("registerForm", form);
            return "auth/register";
        }

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword",
                    "Passwords do not match");
            model.addAttribute("registerForm", form);
            return "auth/register";
        }

        try {
            authClientService.register(form);

            // Auto-login after successful registration
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(form.getUsername(), form.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(auth);

            return "redirect:/";
        } catch (Exception e) {
            log.error("Registration failed for user {}: {}", form.getUsername(), e.getMessage());
            model.addAttribute("errorMessage",
                    "Registration failed: " + e.getMessage());
            model.addAttribute("registerForm", form);
            return "auth/register";
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String profile(Model model) {
        GatewayUserDetails currentUser = SecurityUtils.getCurrentUser();
        model.addAttribute("user", currentUser);
        ProfileFormDto profileForm = new ProfileFormDto(
                currentUser != null ? currentUser.getFullName() : "",
                "",
                "");
        model.addAttribute("profileForm", profileForm);
        return "auth/profile";
    }

    @PostMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String updateProfile(ProfileFormDto profileForm,
                                Model model,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        return "redirect:/profile";
    }
}
