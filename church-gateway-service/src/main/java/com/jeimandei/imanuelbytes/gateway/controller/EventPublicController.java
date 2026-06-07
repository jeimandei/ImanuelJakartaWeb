package com.jeimandei.imanuelbytes.gateway.controller;

import com.jeimandei.imanuelbytes.gateway.dto.EventDto;
import com.jeimandei.imanuelbytes.gateway.service.EventClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/events")
public class EventPublicController {

    private static final Logger log = LoggerFactory.getLogger(EventPublicController.class);

    private final EventClientService eventClientService;

    public EventPublicController(EventClientService eventClientService) {
        this.eventClientService = eventClientService;
    }

    @GetMapping
    public String events(@RequestParam(defaultValue = "0") int page, Model model) {
        try {
            model.addAttribute("events", eventClientService.getUpcomingEvents());
        } catch (Exception e) {
            log.error("Failed to load upcoming events: {}", e.getMessage());
        }
        model.addAttribute("currentPage", page);
        return "public/events";
    }

    @GetMapping("/{slug}")
    public String eventDetail(@PathVariable String slug, Model model) {
        EventDto event = eventClientService.getEventBySlug(slug);
        model.addAttribute("event", event);
        return "public/event-detail";
    }
}
