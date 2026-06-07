package com.jeimandei.imanuelbytes.gateway.controller;

import com.jeimandei.imanuelbytes.gateway.service.GalleryClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/gallery")
public class GalleryPublicController {

    private static final Logger log = LoggerFactory.getLogger(GalleryPublicController.class);

    private final GalleryClientService galleryClientService;

    public GalleryPublicController(GalleryClientService galleryClientService) {
        this.galleryClientService = galleryClientService;
    }

    @GetMapping
    public String gallery(@RequestParam(required = false) String album,
                          @RequestParam(defaultValue = "0") int page,
                          Model model) {
        try {
            model.addAttribute("albumNames", galleryClientService.getAlbumNames());
        } catch (Exception e) {
            log.error("Failed to load album names: {}", e.getMessage());
        }
        try {
            if (album != null && !album.isBlank()) {
                model.addAttribute("galleryItems", galleryClientService.getGalleryByAlbum(album, page, 12));
                model.addAttribute("selectedAlbum", album);
            } else {
                model.addAttribute("galleryItems", galleryClientService.getGalleryItems(page, 12));
            }
            log.debug("Gallery loaded (album={}, page={})", album, page);
        } catch (Exception e) {
            log.error("Failed to load gallery items: {}", e.getMessage());
        }
        model.addAttribute("currentPage", page);
        return "public/gallery";
    }
}
