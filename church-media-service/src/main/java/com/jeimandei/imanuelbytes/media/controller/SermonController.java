package com.jeimandei.imanuelbytes.media.controller;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.common.dto.PageResponse;
import com.jeimandei.imanuelbytes.media.dto.CreateSermonRequest;
import com.jeimandei.imanuelbytes.media.dto.SermonDto;
import com.jeimandei.imanuelbytes.media.dto.UpdateSermonRequest;
import com.jeimandei.imanuelbytes.media.service.SermonService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sermons")
public class SermonController {

    private static final Logger log = LoggerFactory.getLogger(SermonController.class);

    private final SermonService sermonService;

    public SermonController(SermonService sermonService) {
        this.sermonService = sermonService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SermonDto>>> getSermons(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String speaker,
            @RequestParam(required = false) String series,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("Listing sermons: q={}, speaker={}, series={}, page={}, size={}", q, speaker, series, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<SermonDto> resultPage;

        if (q != null && !q.isBlank()) {
            resultPage = sermonService.searchSermons(q, pageable);
        } else if (speaker != null && !speaker.isBlank()) {
            resultPage = sermonService.getSermonsBySpeaker(speaker, pageable);
        } else if (series != null && !series.isBlank()) {
            resultPage = sermonService.getSermonsBySeries(series, pageable);
        } else {
            resultPage = sermonService.getAllSermons(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(resultPage)));
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<SermonDto>>> getLatestSermons(
            @RequestParam(defaultValue = "6") int limit) {
        List<SermonDto> sermons = sermonService.getLatestSermons(limit);
        return ResponseEntity.ok(ApiResponse.success(sermons));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SermonDto>> getSermonById(@PathVariable Long id) {
        log.debug("Fetching sermon by id={}", id);
        SermonDto sermon = sermonService.getSermonById(id);
        return ResponseEntity.ok(ApiResponse.success(sermon));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<SermonDto>> createSermon(
            @Valid @RequestBody CreateSermonRequest request) {
        log.debug("Creating sermon with title='{}'", request.getTitle());
        SermonDto created = sermonService.createSermon(request);
        log.info("Sermon created successfully: id={}, title='{}'", created.getId(), created.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sermon created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<SermonDto>> updateSermon(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSermonRequest request) {
        log.debug("Updating sermon id={}", id);
        SermonDto updated = sermonService.updateSermon(id, request);
        log.info("Sermon updated successfully: id={}", id);
        return ResponseEntity.ok(ApiResponse.success("Sermon updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSermon(@PathVariable Long id) {
        log.debug("Deleting sermon id={}", id);
        sermonService.deleteSermon(id);
        log.info("Sermon deleted successfully: id={}", id);
        return ResponseEntity.ok(ApiResponse.success("Sermon deleted successfully"));
    }
}
