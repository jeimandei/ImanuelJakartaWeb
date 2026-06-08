package com.jeimandei.imanuelbytes.gateway.controller;

import com.jeimandei.imanuelbytes.gateway.dto.NewsArticleDto;
import com.jeimandei.imanuelbytes.gateway.service.CmsClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/news")
public class NewsPublicController {

    private static final Logger log = LoggerFactory.getLogger(NewsPublicController.class);

    private final CmsClientService cmsClientService;

    public NewsPublicController(CmsClientService cmsClientService) {
        this.cmsClientService = cmsClientService;
    }

    @GetMapping
    public String news(@RequestParam(defaultValue = "0") int page, Model model) {
        try {
            model.addAttribute("news", cmsClientService.getPublishedNews(page, 10));
            log.debug("News list loaded (page={})", page);
        } catch (Exception e) {
            log.error("Failed to load published news: {}", e.getMessage());
        }
        model.addAttribute("currentPage", page);
        return "public/news";
    }

    @GetMapping("/{slug}")
    public String newsDetail(@PathVariable String slug, Model model) {
        NewsArticleDto article = cmsClientService.getNewsBySlug(slug);
        model.addAttribute("article", article);
        log.debug("News article detail loaded for slug={}", slug);
        return "public/news-detail";
    }
}
