package com.jeimandei.imanuelbytes.interaction.service.impl;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.interaction.dto.ContactMessageDto;
import com.jeimandei.imanuelbytes.interaction.dto.CreateContactMessageRequest;
import com.jeimandei.imanuelbytes.interaction.dto.UpdateContactStatusRequest;
import com.jeimandei.imanuelbytes.interaction.entity.ContactMessage;
import com.jeimandei.imanuelbytes.interaction.entity.RequestStatus;
import com.jeimandei.imanuelbytes.interaction.repository.ContactMessageRepository;
import com.jeimandei.imanuelbytes.interaction.service.ContactMessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    public ContactMessageServiceImpl(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @Override
    public ContactMessageDto submitContactMessage(CreateContactMessageRequest request) {
        ContactMessage message = new ContactMessage(
                request.getName(),
                request.getEmail(),
                request.getSubject(),
                request.getMessage()
        );
        ContactMessage saved = contactMessageRepository.save(message);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactMessageDto> getAllMessages(Pageable pageable) {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactMessageDto getMessageById(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContactMessage", id));
        return toDto(message);
    }

    @Override
    public ContactMessageDto updateStatus(Long id, UpdateContactStatusRequest request) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContactMessage", id));
        message.setStatus(request.getStatus());
        ContactMessage saved = contactMessageRepository.save(message);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return contactMessageRepository.countByStatus(RequestStatus.NEW);
    }

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    private ContactMessageDto toDto(ContactMessage entity) {
        ContactMessageDto dto = new ContactMessageDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setSubject(entity.getSubject());
        dto.setMessage(entity.getMessage());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
