package com.jeimandei.imanuelbytes.gateway.controller.admin;

import com.jeimandei.imanuelbytes.gateway.dto.NewsArticleDto;
import com.jeimandei.imanuelbytes.gateway.service.CmsClientService;
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
import java.util.Map;

@Controller
@RequestMapping("/admin/news")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
public class AdminNewsController {

    private static final Logger log = LoggerFactory.getLogger(AdminNewsController.class);

    private final CmsClientService cmsClientService;

    public AdminNewsController(CmsClientService cmsClientService) {
        this.cmsClientService = cmsClientService;
    }

    @GetMapping({"", "/"})
    public String listNews(@RequestParam(defaultValue = "0") int page, Model model) {
        String jwt = SecurityUtils.getJwt();
        try {
            model.addAttribute("articles", cmsClientService.getAllNews(page, 10, jwt));
        } catch (Exception e) {
            log.error("Failed to load news: {}", e.getMessage());
            model.addAttribute("articles", com.jeimandei.imanuelbytes.gateway.dto.PageResponse.empty());
        }
        model.addAttribute("currentPage", page);
        return "admin/news/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("newsForm", new NewsArticleDto());
        return "admin/news/form";
    }

    @PostMapping("/create")
    public String createNews(@RequestParam Map<String, String> params,
                             RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> request = new HashMap<>(params);
            cmsClientService.createNewsArticle(request, jwt);
            log.info("News article created successfully");
            redirectAttributes.addFlashAttribute("successMessage", "News article created successfully.");
        } catch (Exception e) {
            log.error("Failed to create news article: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create news article.");
        }
        return "redirect:/admin/news";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            NewsArticleDto article = cmsClientService.getNewsById(id, jwt);
            if (article == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "News article not found.");
                return "redirect:/admin/news";
            }
            model.addAttribute("newsForm", article);
        } catch (Exception e) {
            log.error("Failed to load news article {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to load news article.");
            return "redirect:/admin/news";
        }
        return "admin/news/form";
    }

    @PostMapping("/{id}/edit")
    public String updateNews(@PathVariable Long id,
                             @RequestParam Map<String, String> params,
                             RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> request = new HashMap<>(params);
            cmsClientService.updateNewsArticle(id, request, jwt);
            log.info("News article {} updated successfully", id);
            redirectAttributes.addFlashAttribute("successMessage", "News article updated successfully.");
        } catch (Exception e) {
            log.error("Failed to update news article {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update news article.");
        }
        return "redirect:/admin/news";
    }

    @PostMapping("/{id}/delete")
    public String deleteNews(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            cmsClientService.deleteNewsArticle(id, jwt);
            log.info("News article {} deleted successfully", id);
            redirectAttributes.addFlashAttribute("successMessage", "News article deleted successfully.");
        } catch (Exception e) {
            log.error("Failed to delete news article {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete news article.");
        }
        return "redirect:/admin/news";
    }
}
