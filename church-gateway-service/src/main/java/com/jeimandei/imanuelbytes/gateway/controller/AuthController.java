package com.jeimandei.imanuelbytes.gateway.controller;

import com.jeimandei.imanuelbytes.gateway.dto.LoginFormDto;
import com.jeimandei.imanuelbytes.gateway.dto.ProfileFormDto;
import com.jeimandei.imanuelbytes.gateway.dto.RegisterFormDto;
import com.jeimandei.imanuelbytes.gateway.dto.UserDto;
import com.jeimandei.imanuelbytes.gateway.security.GatewayUserDetails;
import com.jeimandei.imanuelbytes.gateway.service.AuthClientService;
import com.jeimandei.imanuelbytes.gateway.service.InteractionClientService;
import com.jeimandei.imanuelbytes.gateway.service.UserClientService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthClientService authClientService;
    private final AuthenticationManager authenticationManager;
    private final UserClientService userClientService;
    private final InteractionClientService interactionClientService;

    public AuthController(AuthClientService authClientService,
                          AuthenticationManager authenticationManager,
                          UserClientService userClientService,
                          InteractionClientService interactionClientService) {
        this.authClientService = authClientService;
        this.authenticationManager = authenticationManager;
        this.userClientService = userClientService;
        this.interactionClientService = interactionClientService;
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
        if (currentUser != null && currentUser.getEmail() != null) {
            try {
                boolean subscribed = interactionClientService.isSubscribedToNewsletter(currentUser.getEmail());
                model.addAttribute("newsletterSubscribed", subscribed);
            } catch (Exception e) {
                log.debug("Could not check newsletter subscription status: {}", e.getMessage());
                model.addAttribute("newsletterSubscribed", false);
            }
        } else {
            model.addAttribute("newsletterSubscribed", false);
        }
        return "auth/profile";
    }

    @PostMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String updateProfile(ProfileFormDto profileForm,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        return "redirect:/profile";
    }

    @PostMapping("/profile/request-otp")
    @PreAuthorize("isAuthenticated()")
    public String requestPasswordOtp(RedirectAttributes redirectAttributes) {
        GatewayUserDetails currentUser = SecurityUtils.getCurrentUser();
        String jwt = SecurityUtils.getJwt();
        try {
            UserDto user = userClientService.getUserByUsername(currentUser.getUsername(), jwt);
            if (user == null) {
                redirectAttributes.addFlashAttribute("pwErrorMessage", "Could not find your account.");
                return "redirect:/profile#change-password";
            }
            userClientService.requestPasswordOtp(user.getId(), jwt);
            redirectAttributes.addFlashAttribute("otpSent", true);
            redirectAttributes.addFlashAttribute("infoMessage",
                    "OTP sent to " + currentUser.getEmail() + ". Enter it below within 5 minutes.");
        } catch (Exception e) {
            log.error("Failed to send OTP for user {}: {}", currentUser.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("pwErrorMessage",
                    "Failed to send OTP: " + e.getMessage());
        }
        return "redirect:/profile#change-password";
    }

    @PostMapping("/profile/change-password")
    @PreAuthorize("isAuthenticated()")
    public String changePasswordWithOtp(@RequestParam String newPassword,
                                        @RequestParam String confirmPassword,
                                        @RequestParam String otp,
                                        RedirectAttributes redirectAttributes) {
        GatewayUserDetails currentUser = SecurityUtils.getCurrentUser();
        String jwt = SecurityUtils.getJwt();
        try {
            UserDto user = userClientService.getUserByUsername(currentUser.getUsername(), jwt);
            if (user == null) {
                redirectAttributes.addFlashAttribute("pwErrorMessage", "Could not find your account.");
                return "redirect:/profile#change-password";
            }
            userClientService.changePasswordWithOtp(user.getId(),
                    Map.of("newPassword", newPassword,
                           "confirmPassword", confirmPassword,
                           "otp", otp),
                    jwt);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully.");
        } catch (Exception e) {
            log.error("Failed to change password for user {}: {}", currentUser.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("pwErrorMessage",
                    "Failed to change password: " + e.getMessage());
            redirectAttributes.addFlashAttribute("otpSent", true);
        }
        return "redirect:/profile#change-password";
    }

    @PostMapping("/profile/newsletter-subscribe")
    @PreAuthorize("isAuthenticated()")
    public String newsletterSubscribe(RedirectAttributes redirectAttributes) {
        GatewayUserDetails currentUser = SecurityUtils.getCurrentUser();
        String jwt = SecurityUtils.getJwt();
        try {
            interactionClientService.subscribeNewsletter(
                    currentUser.getEmail(), currentUser.getFullName(), jwt);
            redirectAttributes.addFlashAttribute("successMessage",
                    "You have been subscribed to our newsletter. Check your email for a welcome message!");
        } catch (Exception e) {
            log.error("Failed to subscribe user {} to newsletter: {}", currentUser.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to subscribe: " + e.getMessage());
        }
        return "redirect:/profile#newsletter-section";
    }

    @PostMapping("/profile/newsletter-unsubscribe")
    @PreAuthorize("isAuthenticated()")
    public String newsletterUnsubscribe(RedirectAttributes redirectAttributes) {
        GatewayUserDetails currentUser = SecurityUtils.getCurrentUser();
        String jwt = SecurityUtils.getJwt();
        try {
            interactionClientService.requestUnsubscribeConfirmation(currentUser.getEmail(), jwt);
            redirectAttributes.addFlashAttribute("infoMessage",
                    "A confirmation email has been sent to " + currentUser.getEmail() +
                    ". Click the link in the email to confirm your unsubscription.");
        } catch (Exception e) {
            log.error("Failed to request unsubscribe for user {}: {}", currentUser.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to send confirmation: " + e.getMessage());
        }
        return "redirect:/profile#newsletter-section";
    }
}
