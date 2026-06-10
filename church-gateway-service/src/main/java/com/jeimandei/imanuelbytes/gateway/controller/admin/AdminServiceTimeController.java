package com.jeimandei.imanuelbytes.gateway.controller.admin;

import com.jeimandei.imanuelbytes.gateway.dto.ServiceTimeDto;
import com.jeimandei.imanuelbytes.gateway.service.CmsClientService;
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
@RequestMapping("/admin/service-times")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
public class AdminServiceTimeController {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceTimeController.class);

    private static final List<String> DAYS_OF_WEEK = List.of(
            "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY");

    private final CmsClientService cmsClientService;

    public AdminServiceTimeController(CmsClientService cmsClientService) {
        this.cmsClientService = cmsClientService;
    }

    @GetMapping({"", "/"})
    public String list(Model model) {
        String jwt = SecurityUtils.getJwt();
        try {
            model.addAttribute("serviceTimes", cmsClientService.getAllServiceTimes(jwt));
        } catch (Exception e) {
            log.error("Failed to load service times: {}", e.getMessage());
            model.addAttribute("serviceTimes", Collections.emptyList());
        }
        return "admin/service-times/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("serviceTimeForm", new ServiceTimeDto());
        model.addAttribute("daysOfWeek", DAYS_OF_WEEK);
        model.addAttribute("isNew", true);
        return "admin/service-times/form";
    }

    @PostMapping("/create")
    public String create(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            cmsClientService.createServiceTime(buildBody(request), jwt);
            redirectAttributes.addFlashAttribute("successMessage", "Service time created successfully.");
        } catch (Exception e) {
            log.error("Failed to create service time: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create service time: " + e.getMessage());
        }
        return "redirect:/admin/service-times";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            ServiceTimeDto st = cmsClientService.getServiceTimeById(id, jwt);
            if (st == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Service time not found.");
                return "redirect:/admin/service-times";
            }
            model.addAttribute("serviceTimeForm", st);
        } catch (Exception e) {
            log.error("Failed to load service time {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to load service time.");
            return "redirect:/admin/service-times";
        }
        model.addAttribute("daysOfWeek", DAYS_OF_WEEK);
        model.addAttribute("isNew", false);
        return "admin/service-times/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            cmsClientService.updateServiceTime(id, buildBody(request), jwt);
            redirectAttributes.addFlashAttribute("successMessage", "Service time updated successfully.");
        } catch (Exception e) {
            log.error("Failed to update service time {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update service time: " + e.getMessage());
        }
        return "redirect:/admin/service-times";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            cmsClientService.deleteServiceTime(id, jwt);
            redirectAttributes.addFlashAttribute("successMessage", "Service time deleted successfully.");
        } catch (Exception e) {
            log.error("Failed to delete service time {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete service time: " + e.getMessage());
        }
        return "redirect:/admin/service-times";
    }

    private Map<String, Object> buildBody(HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", request.getParameter("name"));
        body.put("dayOfWeek", request.getParameter("dayOfWeek"));
        String startTime = request.getParameter("startTime");
        if (startTime != null && !startTime.isBlank()) body.put("startTime", startTime);
        String endTime = request.getParameter("endTime");
        if (endTime != null && !endTime.isBlank()) body.put("endTime", endTime);
        body.put("location", request.getParameter("location"));
        body.put("description", request.getParameter("description"));
        body.put("active", "true".equalsIgnoreCase(request.getParameter("active")));
        String sortOrder = request.getParameter("sortOrder");
        body.put("sortOrder", (sortOrder != null && !sortOrder.isBlank()) ? Integer.parseInt(sortOrder) : 0);
        return body;
    }
}
