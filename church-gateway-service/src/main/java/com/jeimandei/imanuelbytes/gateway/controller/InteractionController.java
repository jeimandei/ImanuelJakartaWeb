package com.jeimandei.imanuelbytes.gateway.controller;

import com.jeimandei.imanuelbytes.gateway.dto.ContactFormDto;
import com.jeimandei.imanuelbytes.gateway.dto.PrayerRequestFormDto;
import com.jeimandei.imanuelbytes.gateway.service.InteractionClientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class InteractionController {

    private static final Logger log = LoggerFactory.getLogger(InteractionController.class);

    private final InteractionClientService interactionClientService;

    public InteractionController(InteractionClientService interactionClientService) {
        this.interactionClientService = interactionClientService;
    }

    // ─── Prayer Request ───────────────────────────────────────────────────────

    @GetMapping("/prayer-request")
    public String prayerRequestForm(Model model) {
        model.addAttribute("prayerRequestForm", new PrayerRequestFormDto());
        return "public/prayer-request";
    }

    @PostMapping("/prayer-request")
    public String submitPrayerRequest(@Valid PrayerRequestFormDto form,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("prayerRequestForm", form);
            return "public/prayer-request";
        }
        try {
            boolean success = interactionClientService.submitPrayerRequest(form);
            if (success) {
                log.info("Prayer request submitted successfully");
                redirectAttributes.addFlashAttribute("successMessage",
                        "Your prayer request has been submitted. We will be praying for you.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "We could not submit your prayer request at this time. Please try again.");
            }
        } catch (Exception e) {
            log.error("Error submitting prayer request: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "An unexpected error occurred. Please try again later.");
        }
        return "redirect:/prayer-request?submitted=true";
    }

    // ─── Contact ──────────────────────────────────────────────────────────────

    @GetMapping("/contact")
    public String contactForm(Model model) {
        model.addAttribute("contactForm", new ContactFormDto());
        return "public/contact";
    }

    @PostMapping("/contact")
    public String submitContact(@Valid ContactFormDto form,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("contactForm", form);
            return "public/contact";
        }
        try {
            boolean success = interactionClientService.submitContactMessage(form);
            if (success) {
                log.info("Contact message submitted successfully");
                redirectAttributes.addFlashAttribute("successMessage",
                        "Your message has been sent. We will get back to you soon.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "We could not send your message at this time. Please try again.");
            }
        } catch (Exception e) {
            log.error("Error submitting contact message: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "An unexpected error occurred. Please try again later.");
        }
        return "redirect:/contact?submitted=true";
    }

    // ─── Newsletter ───────────────────────────────────────────────────────────

    @PostMapping("/newsletter/subscribe")
    public String subscribeNewsletter(@RequestParam String email,
                                      @RequestParam(required = false) String name,
                                      RedirectAttributes redirectAttributes) {
        try {
            boolean success = interactionClientService.subscribeNewsletter(email, name);
            if (success) {
                log.info("Newsletter subscription successful for email={}", email);
                redirectAttributes.addFlashAttribute("successMessage",
                        "You have been subscribed to our newsletter.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Newsletter subscription failed. Please try again.");
            }
        } catch (Exception e) {
            log.error("Error subscribing newsletter for {}: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "An unexpected error occurred. Please try again later.");
        }
        return "redirect:/";
    }
}
