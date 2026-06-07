package com.jeimandei.imanuelbytes.interaction.controller;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.common.dto.PageResponse;
import com.jeimandei.imanuelbytes.interaction.dto.ContactMessageDto;
import com.jeimandei.imanuelbytes.interaction.dto.CreateContactMessageRequest;
import com.jeimandei.imanuelbytes.interaction.dto.UpdateContactStatusRequest;
import com.jeimandei.imanuelbytes.interaction.service.ContactMessageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact-messages")
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    public ContactMessageController(ContactMessageService contactMessageService) {
        this.contactMessageService = contactMessageService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContactMessageDto>> submitContactMessage(
            @Valid @RequestBody CreateContactMessageRequest request) {
        ContactMessageDto created = contactMessageService.submitContactMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message submitted successfully", created));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ContactMessageDto>>> getAllMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ContactMessageDto> result = contactMessageService.getAllMessages(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ContactMessageDto>> getMessageById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(contactMessageService.getMessageById(id)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ContactMessageDto>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContactStatusRequest request) {
        ContactMessageDto updated = contactMessageService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Message status updated", updated));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        return ResponseEntity.ok(ApiResponse.success(contactMessageService.getUnreadCount()));
    }
}
