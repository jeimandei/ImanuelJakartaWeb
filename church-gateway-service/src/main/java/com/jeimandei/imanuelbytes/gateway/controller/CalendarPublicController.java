package com.jeimandei.imanuelbytes.gateway.controller;

import com.jeimandei.imanuelbytes.gateway.dto.EventDto;
import com.jeimandei.imanuelbytes.gateway.service.EventClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Public calendar page and its JSON event feed.
 *
 * <p>The calendar is a visualization of the existing published events from
 * church-event-service — it owns no data of its own.  The {@code /calendar/feed}
 * endpoint returns events in the shape expected by FullCalendar.</p>
 */
@Controller
public class CalendarPublicController {

    private static final Logger log = LoggerFactory.getLogger(CalendarPublicController.class);

    private final EventClientService eventClientService;

    public CalendarPublicController(EventClientService eventClientService) {
        this.eventClientService = eventClientService;
    }

    @GetMapping("/calendar")
    public String calendar() {
        return "public/calendar";
    }

    @GetMapping(value = "/calendar/feed", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Map<String, Object>> feed() {
        List<EventDto> events = eventClientService.getPublishedEvents(500);
        List<Map<String, Object>> result = new ArrayList<>();
        for (EventDto event : events) {
            if (event.getEventStart() == null) {
                continue;
            }
            Map<String, Object> entry = new HashMap<>();
            entry.put("title", event.getTitle());
            entry.put("start", event.getEventStart().toString());
            if (event.getEventEnd() != null) {
                entry.put("end", event.getEventEnd().toString());
            }
            if (event.getSlug() != null) {
                entry.put("url", "/events/" + event.getSlug());
            }
            result.add(entry);
        }
        log.debug("Calendar feed served {} events", result.size());
        return result;
    }
}
