package com.jeimandei.imanuelbytes.gateway.controller.admin;

import com.jeimandei.imanuelbytes.gateway.dto.GalleryItemDto;
import com.jeimandei.imanuelbytes.gateway.service.GalleryClientService;
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
@RequestMapping("/admin/gallery")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminGalleryController {

    private static final Logger log = LoggerFactory.getLogger(AdminGalleryController.class);

    private final GalleryClientService galleryClientService;

    public AdminGalleryController(GalleryClientService galleryClientService) {
        this.galleryClientService = galleryClientService;
    }

    @GetMapping({"", "/"})
    public String listGallery(@RequestParam(defaultValue = "0") int page, Model model) {
        try {
            model.addAttribute("galleryItems", galleryClientService.getGalleryItems(page, 20));
            model.addAttribute("albumNames", galleryClientService.getAlbumNames());
        } catch (Exception e) {
            log.error("Failed to load gallery: {}", e.getMessage());
        }
        model.addAttribute("currentPage", page);
        return "admin/gallery/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("galleryForm", new GalleryItemDto());
        return "admin/gallery/form";
    }

    @PostMapping({"/create", ""})
    public String createGalleryItem(@RequestParam Map<String, String> params,
                                    RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> request = new HashMap<>(params);
            if (params.containsKey("active")) {
                request.put("active", Boolean.parseBoolean(params.get("active")));
            } else {
                request.put("active", true);
            }
            if (params.containsKey("displayOrder")) {
                try {
                    request.put("displayOrder", Integer.parseInt(params.get("displayOrder")));
                } catch (NumberFormatException ignored) {
                    request.put("displayOrder", 0);
                }
            }
            galleryClientService.createGalleryItem(request, jwt);
            log.info("Gallery item created successfully");
            redirectAttributes.addFlashAttribute("successMessage", "Gallery item added successfully.");
        } catch (Exception e) {
            log.error("Failed to create gallery item: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add gallery item: " + e.getMessage());
        }
        return "redirect:/admin/gallery";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            GalleryItemDto item = galleryClientService.getGalleryItemById(id, jwt);
            if (item == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Gallery item not found.");
                return "redirect:/admin/gallery";
            }
            model.addAttribute("galleryForm", item);
        } catch (Exception e) {
            log.error("Failed to load gallery item {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to load gallery item.");
            return "redirect:/admin/gallery";
        }
        return "admin/gallery/form";
    }

    @PostMapping("/{id}/edit")
    public String updateGalleryItem(@PathVariable Long id,
                                    @RequestParam Map<String, String> params,
                                    RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            Map<String, Object> request = new HashMap<>(params);
            if (params.containsKey("active")) {
                request.put("active", Boolean.parseBoolean(params.get("active")));
            }
            if (params.containsKey("displayOrder")) {
                try {
                    request.put("displayOrder", Integer.parseInt(params.get("displayOrder")));
                } catch (NumberFormatException ignored) {
                    request.put("displayOrder", 0);
                }
            }
            galleryClientService.updateGalleryItem(id, request, jwt);
            log.info("Gallery item {} updated successfully", id);
            redirectAttributes.addFlashAttribute("successMessage", "Gallery item updated successfully.");
        } catch (Exception e) {
            log.error("Failed to update gallery item {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update gallery item.");
        }
        return "redirect:/admin/gallery";
    }

    @PostMapping("/{id}/delete")
    public String deleteGalleryItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String jwt = SecurityUtils.getJwt();
        try {
            galleryClientService.deleteGalleryItem(id, jwt);
            log.info("Gallery item {} deleted successfully", id);
            redirectAttributes.addFlashAttribute("successMessage", "Gallery item deleted successfully.");
        } catch (Exception e) {
            log.error("Failed to delete gallery item {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete gallery item.");
        }
        return "redirect:/admin/gallery";
    }
}
