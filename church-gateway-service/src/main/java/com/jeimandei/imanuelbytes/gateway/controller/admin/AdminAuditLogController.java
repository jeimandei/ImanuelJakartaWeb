package com.jeimandei.imanuelbytes.gateway.controller.admin;

import com.jeimandei.imanuelbytes.gateway.service.AuditClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/admin/audit-logs")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminAuditLogController {

    private static final Logger log = LoggerFactory.getLogger(AdminAuditLogController.class);

    private final AuditClientService auditClientService;

    public AdminAuditLogController(AuditClientService auditClientService) {
        this.auditClientService = auditClientService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String actor,
                       @RequestParam(required = false) String action,
                       @RequestParam(required = false) String entityType,
                       @RequestParam(required = false) String serviceName,
                       @RequestParam(required = false) String from,
                       @RequestParam(required = false) String to,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        log.debug("Audit log query: actor={}, action={}, entityType={}, service={}", actor, action, entityType, serviceName);
        try {
            Map<String, Object> result = auditClientService.queryAuditLogs(
                    actor, action, entityType, serviceName, from, to, page, 50);
            model.addAttribute("auditPage", result);
            model.addAttribute("logs", result.get("content"));
            model.addAttribute("totalPages", result.get("totalPages"));
            model.addAttribute("currentPage", result.get("number"));
            model.addAttribute("totalElements", result.get("totalElements"));
        } catch (Exception e) {
            log.error("Failed to load audit logs: {}", e.getMessage());
            model.addAttribute("logs", java.util.Collections.emptyList());
        }
        // Pass filter values back for form repopulation
        model.addAttribute("filterActor", actor);
        model.addAttribute("filterAction", action);
        model.addAttribute("filterEntityType", entityType);
        model.addAttribute("filterServiceName", serviceName);
        model.addAttribute("filterFrom", from);
        model.addAttribute("filterTo", to);
        model.addAttribute("currentPage", page);
        return "admin/audit-logs/index";
    }
}
