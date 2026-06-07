package com.jeimandei.imanuelbytes.gateway.controller;

import com.jeimandei.imanuelbytes.gateway.dto.SermonDto;
import com.jeimandei.imanuelbytes.gateway.service.SermonClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/sermons")
public class SermonPublicController {

    private static final Logger log = LoggerFactory.getLogger(SermonPublicController.class);

    private final SermonClientService sermonClientService;

    public SermonPublicController(SermonClientService sermonClientService) {
        this.sermonClientService = sermonClientService;
    }

    @GetMapping
    public String sermons(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "9") int size,
                          @RequestParam(required = false) String speaker,
                          @RequestParam(required = false) String series,
                          Model model) {
        try {
            model.addAttribute("sermons", sermonClientService.getAllSermons(page, size, null));
            log.debug("Sermon list loaded (page={}, size={})", page, size);
        } catch (Exception e) {
            log.error("Failed to load sermons list: {}", e.getMessage());
        }
        model.addAttribute("currentPage", page);
        model.addAttribute("speaker", speaker);
        model.addAttribute("series", series);
        return "public/sermons";
    }

    @GetMapping("/{id}")
    public String sermonDetail(@PathVariable Long id, Model model) {
        SermonDto sermon = sermonClientService.getSermonById(id);
        model.addAttribute("sermon", sermon);
        log.debug("Sermon detail loaded for id={}", id);
        return "public/sermon-detail";
    }
}
